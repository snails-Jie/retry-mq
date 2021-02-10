package io.github.zj;

import io.github.zj.common.SubscriptionData;
import io.github.zj.common.protocol.header.PullMessageRequestHeader;
import io.github.zj.common.protocol.heartbeat.MessageModel;
import io.github.zj.config.ClientConfig;
import io.github.zj.exception.MQClientException;
import io.github.zj.factory.MQClientInstance;
import io.github.zj.impl.MQClientManager;
import io.github.zj.listener.MessageListener;
import io.github.zj.message.MessageQueue;
import io.github.zj.rebalance.AllocateMessageQueueAveragely;
import io.github.zj.store.OffsetStore;
import io.github.zj.store.ReadOffsetType;
import io.github.zj.store.RemoteBrokerOffsetStore;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName DefaultMQPushConsumer
 * @Description: MQ推模式消费端
 * @author: zhangjie
 * @Date: 2021/2/3 15:06
 **/
public class DefaultMQPushConsumer extends ClientConfig implements MQPushConsumer {
    private MQClientInstance mQClientFactory;

    private String consumerGroup;

    private MessageModel messageModel = MessageModel.CLUSTERING;

    private final RebalancePushImpl rebalanceImpl = new RebalancePushImpl(this);

    private AllocateMessageQueueStrategy allocateMessageQueueStrategy;

    private OffsetStore offsetStore;

    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

    /**
     * 消费超时时间
     */
    private long consumeTimeout = 15;

    /**
     * 流控阈值，每个消息队列默认最多缓存1000条消息
     */
    private int pullThresholdForQueue = 1000;
    /**
     * 每个消息队列默认将最多缓存100 MiB
     */
    private int pullThresholdSizeForQueue = 100;
    /**
     * 触发流控后延迟拉取间隔
     */
    private static final long PULL_TIME_DELAY_MILLS_WHEN_FLOW_CONTROL = 50;
    /**
     * 同时最大跨度偏移量。它对连续消费没有影响
     */
    private int consumeConcurrentlyMaxSpan = 2000;

    /**
     * 发生异常（订阅信息不存在）延迟拉取间隔
     */
    private long pullTimeDelayMillsWhenException = 3000;

    /**
     * 批量消费大小
     */
    private int consumeMessageBatchMaxSize = 1;

    /**
     * 批量拉取消息数量
     */
    private int pullBatchSize = 32;

    /**
     * Minimum consumer thread number
     */
    private int consumeThreadMin = 20;

    /**
     * Max consumer thread number
     */
    private int consumeThreadMax = 20;

    private ConsumeMessageService consumeMessageService;

    private MessageListener messageListenerInner;


    public DefaultMQPushConsumer(final String consumerGroup){
        this(consumerGroup,new AllocateMessageQueueAveragely());
    }

    public DefaultMQPushConsumer(final String consumerGroup,AllocateMessageQueueStrategy allocateMessageQueueStrategy){
        this.consumerGroup = consumerGroup;
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;

    }

    @Override
    public void start() throws MQClientException {
        this.changeInstanceNameToPID();
        this.mQClientFactory = MQClientManager.getInstance().getOrCreateMQClientInstance(this);

        /** 配置消费进度管理 start */
        this.offsetStore = new RemoteBrokerOffsetStore(this.mQClientFactory,consumerGroup);
        /** 配置消费进度管理 end */

        /** 配置队列负载均衡配置 start */
        rebalanceImpl.setMQClientFactory(mQClientFactory);
        rebalanceImpl.setConsumerGroup(consumerGroup);
        rebalanceImpl.setMessageModel(messageModel);
        rebalanceImpl.setAllocateMessageQueueStrategy(allocateMessageQueueStrategy);
        /** 设置队列负载均衡配置 end */

        /** 消息消费线程 */
        if(this.getMessageListenerInner() instanceof MessageListener){
            this.consumeMessageService =
                    new ConsumeMessageConcurrentlyService(this, this.getMessageListenerInner());
        }

        boolean registerOK = mQClientFactory.registerConsumer( this.consumerGroup, this);
        if (!registerOK) {
            throw new MQClientException("The consumer group[" + this.consumerGroup + "] has been created before, specify another name please.");
        }
        mQClientFactory.start();

    }


    @Override
    public void doRebalance() {
        this.rebalanceImpl.doRebalance();
    }

    @Override
    public void subscribe(String topic){
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setTopic(topic);
        this.rebalanceImpl.getSubscriptionInner().put(topic,subscriptionData);
    }

    @Override
    public Set<SubscriptionData> subscriptions() {
        Set<SubscriptionData> subSet = new HashSet<SubscriptionData>();
        subSet.addAll(this.rebalanceImpl.getSubscriptionInner().values());
        return subSet;
    }

    @Override
    public void updateTopicSubscribeInfo(String topic, List<MessageQueue> info) {
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();
        if (subTable != null) {
            if (subTable.containsKey(topic)) {
                this.rebalanceImpl.topicSubscribeInfoTable.put(topic, info);
            }
        }
    }

    /**
     * 拉取队列消息
     * @param pullRequest
     */
    public void pullMessage(final PullRequest pullRequest) {
        final ProcessQueue processQueue = pullRequest.getProcessQueue();
        pullRequest.getProcessQueue().setLastPullTimestamp(System.currentTimeMillis());

        //初始化为0
        long cachedMessageCount = processQueue.getMsgCount().get();
        long cachedMessageSizeInMiB = processQueue.getMsgSize().get() / (1024 * 1024);

        //流控限制 -> 延迟拉取
        if (cachedMessageCount > this.getPullThresholdForQueue()) {
            this.executePullRequestLater(pullRequest, PULL_TIME_DELAY_MILLS_WHEN_FLOW_CONTROL);
            return;
        }
        if(cachedMessageSizeInMiB > this.getPullThresholdSizeForQueue()){
            this.executePullRequestLater(pullRequest, PULL_TIME_DELAY_MILLS_WHEN_FLOW_CONTROL);
            return;
        }
        //队列消费进度跨度不能超过阈值
        if (processQueue.getMaxSpan() > this.consumeConcurrentlyMaxSpan) {
            this.executePullRequestLater(pullRequest, PULL_TIME_DELAY_MILLS_WHEN_FLOW_CONTROL);
        }

        final SubscriptionData subscriptionData = this.rebalanceImpl.getSubscriptionInner().get(pullRequest.getMessageQueue().getTopic());
        if (null == subscriptionData) {
            this.executePullRequestLater(pullRequest, pullTimeDelayMillsWhenException);
            System.out.println("find the consumer's subscription failed");
            return;
        }

        // 为了统计响应时间等监控指标：RT、TPS（待实现）
        final long beginTimestamp = System.currentTimeMillis();

        PullCallback pullCallback = new PullCallback() {
            @Override
            public void onSuccess(PullResult pullResult) {
                pullRequest.setNextOffset(pullResult.getNextBeginOffset());
                if (pullResult.getMsgFoundList() != null && !pullResult.getMsgFoundList().isEmpty()) {
                    boolean dispatchToConsume = processQueue.putMessage(pullResult.getMsgFoundList());
                    consumeMessageService.submitConsumeRequest(
                            pullResult.getMsgFoundList(),
                            processQueue,
                            pullRequest.getMessageQueue(),
                            dispatchToConsume);
                }
                executePullRequestImmediately(pullRequest);
            }

            @Override
            public void onException(Throwable e) {
                executePullRequestLater(pullRequest, pullTimeDelayMillsWhenException);
            }
        };

        /**
         * (未理解其意思，暂未实现)
         * commitOffsetValue -> commitOffsetEnable -> sysFlag
         */
        boolean commitOffsetEnable = false;
        long commitOffsetValue =  this.offsetStore.readOffset(pullRequest.getMessageQueue(), ReadOffsetType.READ_FROM_MEMORY);
        if (commitOffsetValue > 0) {
            commitOffsetEnable = true;
        }

        // 向broker(mysql)拉取消息
        PullMessageRequestHeader requestHeader = new PullMessageRequestHeader();
        requestHeader.setQueueOffset(pullRequest.getNextOffset());
        requestHeader.setTopic(pullRequest.getMessageQueue().getTopic());
        requestHeader.setConsumerGroup(pullRequest.getConsumerGroup());
        requestHeader.setQueueId(pullRequest.getMessageQueue().getQueueId());
        requestHeader.setBatchPullSize(pullBatchSize);
        this.mQClientFactory.getClientApi().pullMessage(requestHeader,pullCallback);

    }

    private void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {
        this.mQClientFactory.getPullMessageService().executePullRequestLater(pullRequest, timeDelay);
    }

    public void executePullRequestImmediately(final PullRequest pullRequest) {
        this.mQClientFactory.getPullMessageService().executePullRequestImmediately(pullRequest);
    }

    public ConcurrentMap<String, SubscriptionData> getSubscriptionInner() {
        return this.rebalanceImpl.getSubscriptionInner();
    }

    public MessageListener getMessageListenerInner() {
        return messageListenerInner;
    }

    public void setMessageListenerInner(MessageListener messageListenerInner) {
        this.messageListenerInner = messageListenerInner;
    }

    public int getConsumeMessageBatchMaxSize() {
        return consumeMessageBatchMaxSize;
    }

    public int getConsumeThreadMin() {
        return consumeThreadMin;
    }

    public void setConsumeThreadMin(int consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }

    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }

    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public OffsetStore getOffsetStore() {
        return offsetStore;
    }

    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public int getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }

    public void setPullThresholdForQueue(int pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }

    public int getPullThresholdSizeForQueue() {
        return pullThresholdSizeForQueue;
    }

    public void setPullThresholdSizeForQueue(int pullThresholdSizeForQueue) {
        this.pullThresholdSizeForQueue = pullThresholdSizeForQueue;
    }

    public int getPullBatchSize() {
        return pullBatchSize;
    }

    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }

    public long getConsumeTimeout() {
        return consumeTimeout;
    }

    public void setConsumeTimeout(final long consumeTimeout) {
        this.consumeTimeout = consumeTimeout;
    }
}
