package io.github.zj;

import io.github.zj.common.SubscriptionData;
import io.github.zj.common.protocol.heartbeat.MessageModel;
import io.github.zj.factory.MQClientInstance;
import io.github.zj.message.MessageQueue;

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

    /** topic -> 主题的订阅数据 */
    protected final ConcurrentMap<String, SubscriptionData> subscriptionInner = new ConcurrentHashMap<String, SubscriptionData>();

    /** topic -> 队列列表（定时任务获取） */
    protected final ConcurrentMap<String, List<MessageQueue>> topicSubscribeInfoTable = new ConcurrentHashMap<String, List<MessageQueue>>();

    protected final ConcurrentMap<MessageQueue, ProcessQueue> processQueueTable = new ConcurrentHashMap<MessageQueue, ProcessQueue>(64);

    protected MessageModel messageModel;
    protected MQClientInstance mQClientFactory;
    protected String consumerGroup;
    protected AllocateMessageQueueStrategy allocateMessageQueueStrategy;




    public ConcurrentMap<String, SubscriptionData> getSubscriptionInner() {
        return subscriptionInner;
    }

    public RebalancePushImpl(MessageModel messageModel) {
        this.messageModel = messageModel;
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
                }
                break;
            }
            default:
                break;
        }
    }

    private boolean updateProcessQueueTableInRebalance(final String topic, final List<MessageQueue> mqList){
        boolean changed = false;
        List<PullRequest> pullRequestList = new ArrayList<PullRequest>();
        for (MessageQueue mq : mqList) {
            if (!this.processQueueTable.containsKey(mq)) {
                //删除脏的进度：offset
                ProcessQueue pq = new ProcessQueue();
                //获取队列的起始消费的进度：nextOffset


            }
        }
        return changed;
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
}
