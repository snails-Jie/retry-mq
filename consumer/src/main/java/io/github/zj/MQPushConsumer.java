package io.github.zj;

import io.github.zj.common.SubscriptionData;
import io.github.zj.exception.MQClientException;
import io.github.zj.message.MessageQueue;

import java.util.List;
import java.util.Set;

/**
 * Push consumer
 * @author zhangjie
 */
public interface MQPushConsumer {

    /**
     * Start the consumer
     */
    void start() throws MQClientException;

    /**
     * 队列负载
     */
    void doRebalance();

    /**
     * 订阅主题
     * @param topic
     */
    void subscribe(final String topic);

    /**
     * 获取所有订阅数据
     * @see subscribe(final String topic)
     * @return
     */
    Set<SubscriptionData> subscriptions();

    /**
     * 更新主题下的队列信息
     * @param topic
     * @param info
     */
    void updateTopicSubscribeInfo(final String topic, final List<MessageQueue> info);
}
