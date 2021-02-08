package io.github.zj.message;

import java.util.Objects;

/**
 * @ClassName MessageQueue
 * @Description: 消息队列
 * @author: zhangjie
 * @Date: 2021/2/3 22:15
 **/
public class MessageQueue implements Comparable<MessageQueue> {
    private String topic;
    private String brokerName;
    private Integer queueId;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageQueue)) return false;
        MessageQueue that = (MessageQueue) o;
        return topic.equals(that.topic) &&
                brokerName.equals(that.brokerName) &&
                queueId.equals(that.queueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, brokerName, queueId);
    }

    @Override
    public int compareTo(MessageQueue o) {
        {
            int result = this.topic.compareTo(o.topic);
            if (result != 0) {
                return result;
            }
        }

        {
            int result = this.brokerName.compareTo(o.brokerName);
            if (result != 0) {
                return result;
            }
        }

        return this.queueId - o.queueId;
    }
}
