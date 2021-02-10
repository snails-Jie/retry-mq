package io.github.zj.common.protocol.header;

/**
 * @ClassName PullMessageRequestHeader
 * @Description: 拉取消息请求实体类
 * @author: zhangjie
 * @Date: 2021/2/9 11:10
 **/
public class PullMessageRequestHeader {
    private String topic;
    private String consumerGroup;
    private Integer queueId;
    private Long queueOffset;

    private Integer batchPullSize;

    public Integer getBatchPullSize() {
        return batchPullSize;
    }

    public void setBatchPullSize(Integer batchPullSize) {
        this.batchPullSize = batchPullSize;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Long getQueueOffset() {
        return queueOffset;
    }

    public void setQueueOffset(Long queueOffset) {
        this.queueOffset = queueOffset;
    }
}
