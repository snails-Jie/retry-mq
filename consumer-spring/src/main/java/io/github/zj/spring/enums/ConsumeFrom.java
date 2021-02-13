package io.github.zj.spring.enums;

/**
 * consumer的消费策略
 * @author zhangjie
 */
public enum ConsumeFrom {
    // CONSUME_FROM_FIRST_OFFSET 从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
    EARLIEST("earliest"),
    // CONSUME_FROM_LAST_OFFSET 默认策略，从该队列最尾开始消费，即跳过历史消息
    LATEST("latest");

    private String name;

    ConsumeFrom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
