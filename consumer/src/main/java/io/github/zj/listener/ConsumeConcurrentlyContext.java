package io.github.zj.listener;

import io.github.zj.message.MessageQueue;

/**
 * @ClassName ConsumeConcurrentlyContext
 * @Description: 消费消息的上下文
 * @author: zhangjie
 * @Date: 2021/2/10 19:56
 **/
public class ConsumeConcurrentlyContext {
    private final MessageQueue messageQueue;

    public ConsumeConcurrentlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }
}
