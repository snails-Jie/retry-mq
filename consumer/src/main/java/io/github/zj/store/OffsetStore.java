package io.github.zj.store;

import io.github.zj.message.MessageQueue;

/**
 * 消费进度接口
 * @author zhangjie
 */
public interface OffsetStore {

    /**
     * 删除队列的消费进度
     * @param mq
     */
    void removeOffset(MessageQueue mq);

    /**
     * 获取队列的消费进度
     * @param mq
     * @param type
     * @return
     */
    long readOffset(final MessageQueue mq, final ReadOffsetType type);

    /**
     * 更新消费进度，存储在内存中
     */
    void updateOffset(final MessageQueue mq, final long offset);
}
