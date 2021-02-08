package io.github.zj;


import io.github.zj.message.MessageQueue;

import java.util.List;

/**
 * 分配消息的策略算法
 * @author zhangjie
 */
public interface AllocateMessageQueueStrategy {

    /**
     * 根据消费者ID进行分配
     * @param consumerGroup
     * @param currentCID
     * @param mqAll
     * @param cidAll
     * @return
     */
    List<MessageQueue> allocate(
            final String consumerGroup,
            final String currentCID,
            final List<MessageQueue> mqAll,
            final List<String> cidAll
    );
}
