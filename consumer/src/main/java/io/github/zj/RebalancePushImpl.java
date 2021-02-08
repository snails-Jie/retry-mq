package io.github.zj;

import io.github.zj.common.SubscriptionData;
import io.github.zj.common.protocol.heartbeat.MessageModel;
import io.github.zj.factory.MQClientInstance;
import io.github.zj.message.MessageQueue;
import io.github.zj.store.OffsetStore;
import io.github.zj.store.ReadOffsetType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName RebalancePushImpl
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/3 18:16
 **/
public class RebalancePushImpl {

    private final DefaultMQPushConsumer defaultMQPushConsumer;

    /** topic -> 主题的订阅数据 */
    protected final ConcurrentMap<String, SubscriptionData> subscriptionInner = new ConcurrentHashMap<String, SubscriptionData>();

    /** topic -> 队列列表（定时任务获取） */
    protected final ConcurrentMap<String, List<MessageQueue>> topicSubscribeInfoTable = new ConcurrentHashMap<String, List<MessageQueue>>();

    /** 处理分配到的队列 */
    protected final ConcurrentMap<MessageQueue, ProcessQueue> processQueueTable = new ConcurrentHashMap<MessageQueue, ProcessQueue>(64);

    protected MessageModel messageModel;
    protected MQClientInstance mQClientFactory;
    protected String consumerGroup;
    protected AllocateMessageQueueStrategy allocateMessageQueueStrategy;

    public ConcurrentMap<String, SubscriptionData> getSubscriptionInner() {
        return subscriptionInner;
    }

    public RebalancePushImpl(DefaultMQPushConsumer defaultMQPushConsumer) {
        this.defaultMQPushConsumer = defaultMQPushConsumer;
    }

    public void doRebalance() {
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();
        if (subTable != null) {
            for (final Map.Entry<String, SubscriptionData> entry : subTable.entrySet()) {
                final String topic = entry.getKey();
                this.rebalanceByTopic(topic);
            }
        }
    }

    private void rebalanceByTopic(final String topic) {
        switch (messageModel) {
            case CLUSTERING:{
                List<MessageQueue> mqAll = this.topicSubscribeInfoTable.get(topic);
                List<String> cidAll = this.mQClientFactory.findConsumerIdList(consumerGroup);
                if(mqAll !=null && cidAll !=null ){
                    Collections.sort(mqAll);
                    Collections.sort(cidAll);

                    AllocateMessageQueueStrategy strategy = this.allocateMessageQueueStrategy;

                    List<MessageQueue> allocateResult = null;
                    try {
                        allocateResult = strategy.allocate(
                                this.consumerGroup,
                                this.mQClientFactory.getClientId(),
                                mqAll,
                                cidAll);
                    } catch (Throwable e) {
                        System.out.println("AllocateMessageQueueStrategy.allocate Exception."+ e);
                        return;
                    }

                    Set<MessageQueue> allocateResultSet = new HashSet<MessageQueue>();
                    if (allocateResult != null) {
                        allocateResultSet.addAll(allocateResult);
                    }
                    boolean changed = this.updateProcessQueueTableInRebalance(allocateResultSet);
                    if(changed){
                        this.messageQueueChanged(topic, mqAll, allocateResultSet);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    //暂未实现
    public void messageQueueChanged(String topic, List<MessageQueue> mqAll, Set<MessageQueue> mqDivided){
        /**
         * 当rebalance结果改变时，应该更新订阅版本以通知代理。
         * 修正:不一致的订阅可能导致消费者错过消息。
         */
    }


    /**
     * 更新消费进度，并添加消息拉取任务
     * @param allocateResultSet
     * @return
     */
    private boolean updateProcessQueueTableInRebalance(Set<MessageQueue> allocateResultSet){
        boolean changed = false;
        List<PullRequest> pullRequestList = new ArrayList<PullRequest>();
        for (MessageQueue mq : allocateResultSet) {
            if (!this.processQueueTable.containsKey(mq)) {
                this.removeDirtyOffset(mq);
                ProcessQueue pq = new ProcessQueue();
                //获取队列的起始消费的进度：nextOffset
                long nextOffset = this.computePullFromWhere(mq);
                if (nextOffset >= 0) {
                    ProcessQueue pre = this.processQueueTable.putIfAbsent(mq, pq);
                    if (pre != null) {
                        System.out.println(String.format("doRebalance, %s, mq already exists, %s",consumerGroup, mq));
                    }else{
                        System.out.println(String.format("doRebalance, %s, add a new mq, %s",consumerGroup, mq));
                        PullRequest pullRequest = new PullRequest();
                        pullRequest.setConsumerGroup(consumerGroup);
                        pullRequest.setNextOffset(nextOffset);
                        pullRequest.setMessageQueue(mq);
                        pullRequest.setProcessQueue(pq);
                        pullRequestList.add(pullRequest);
                        changed = true;
                    }
                }else{
                    System.out.println(String.format("doRebalance, %s, add new mq failed, %s",consumerGroup, mq));
                }

            }
        }

        this.dispatchPullRequest(pullRequestList);
        return changed;
    }

    /**
     * 派发队列拉取消息任务
     *   -->触发PullMessageService线程
     * @param pullRequestList
     */
    public void dispatchPullRequest(List<PullRequest> pullRequestList) {
        for (PullRequest pullRequest : pullRequestList) {
            this.defaultMQPushConsumer.executePullRequestImmediately(pullRequest);
            System.out.println(String.format("doRebalance, %s, add a new pull request %s",consumerGroup,pullRequest));
        }
    }

    /**
     * 队列拉取的起始位置
     * @param mq
     * @return
     */
    public long computePullFromWhere(MessageQueue mq) {
        long result = -1;

        final ConsumeFromWhere consumeFromWhere = this.defaultMQPushConsumer.getConsumeFromWhere();
        final OffsetStore offsetStore = this.defaultMQPushConsumer.getOffsetStore();
        switch (consumeFromWhere) {
            case CONSUME_FROM_FIRST_OFFSET: {
                long lastOffset = offsetStore.readOffset(mq, ReadOffsetType.READ_FROM_STORE);
                if (lastOffset >= 0) {
                    result = lastOffset;
                }else if (-1 == lastOffset) {
                    result = 0L;
                } else {
                    result = -1;
                }
                break;
            }
            default:
                break;
        }
        return result;
    }

    /** 先删后增 */
    public void removeDirtyOffset(final MessageQueue mq) {
        this.defaultMQPushConsumer.getOffsetStore().removeOffset(mq);
    }


    public void setMQClientFactory(MQClientInstance mQClientFactory) {
        this.mQClientFactory = mQClientFactory;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public void setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }
}
