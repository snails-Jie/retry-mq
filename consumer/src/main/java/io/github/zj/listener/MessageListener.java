package io.github.zj.listener;

import io.github.zj.message.MessageExt;

import java.util.List;

/**
 * @author zhangjie
 */
public interface MessageListener {
    /**
     * 不建议抛出异常，而是返回consumercurrentstatus。
     * 如果消费失败,RECONSUME_LATER
     * @param msgs
     * @param context
     * @return
     */
    ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
                                             final ConsumeConcurrentlyContext context);
}
