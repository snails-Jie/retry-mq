package io.github.zj.message;

/**
 * 消费组的元数据
 * @author zhangjie
 */
public class ConsumerGroupMetadata {
    private String bindingTopic;
    private String consumeFrom;
    private String broadcast;

    public String getBindingTopic() {
        return bindingTopic;
    }

    public void setBindingTopic(String bindingTopic) {
        this.bindingTopic = bindingTopic;
    }

    public String getConsumeFrom() {
        return consumeFrom;
    }

    public void setConsumeFrom(String consumeFrom) {
        this.consumeFrom = consumeFrom;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }
}
