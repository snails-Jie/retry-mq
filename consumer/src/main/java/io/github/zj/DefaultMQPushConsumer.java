package io.github.zj;

import io.github.zj.common.SubscriptionData;
import io.github.zj.common.protocol.heartbeat.MessageModel;
import io.github.zj.config.ClientConfig;
import io.github.zj.exception.MQClientException;
import io.github.zj.factory.MQClientInstance;
import io.github.zj.impl.MQClientManager;
import io.github.zj.message.MessageQueue;

import java.util.HashSet;
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

    private final RebalancePushImpl rebalanceImpl = new RebalancePushImpl(messageModel);

    public DefaultMQPushConsumer(final String consumerGroup){
        this.consumerGroup = consumerGroup;
    }

    @Override
    public void start() throws MQClientException {
        if (this.getMessageModel() == MessageModel.CLUSTERING) {
            this.changeInstanceNameToPID();
        }
        this.mQClientFactory = MQClientManager.getInstance().getOrCreateMQClientInstance(this);

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
    public void updateTopicSubscribeInfo(String topic, Set<MessageQueue> info) {
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();
        if (subTable != null) {
            if (subTable.containsKey(topic)) {
                this.rebalanceImpl.topicSubscribeInfoTable.put(topic, info);
            }
        }
    }

    public ConcurrentMap<String, SubscriptionData> getSubscriptionInner() {
        return this.rebalanceImpl.getSubscriptionInner();
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }
}
