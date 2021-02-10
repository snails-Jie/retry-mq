package io.github.zj;

import io.github.zj.message.MessageExt;
import io.github.zj.message.MessageQueue;

import java.util.List;

/**
 * @author zhangjie
 */
public interface ConsumeMessageService {


    /**
     * 提交消息
     * @param msgs
     * @param processQueue
     * @param messageQueue
     * @param dispathToConsume
     */
    void submitConsumeRequest(
            final List<MessageExt> msgs,
            final ProcessQueue processQueue,
            final MessageQueue messageQueue,
            final boolean dispathToConsume);
}
