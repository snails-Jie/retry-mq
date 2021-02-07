package io.github.zj;

import io.github.zj.common.SubscriptionData;
import io.github.zj.common.protocol.heartbeat.MessageModel;
import io.github.zj.message.MessageQueue;

import java.util.Map;
import java.util.Set;
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
    protected final ConcurrentMap<String, Set<MessageQueue>> topicSubscribeInfoTable = new ConcurrentHashMap<String, Set<MessageQueue>>();

    protected MessageModel messageModel;

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
                Set<MessageQueue> mqSet = this.topicSubscribeInfoTable.get(topic);
                break;
            }
            default:
                break;
        }
    }
}
