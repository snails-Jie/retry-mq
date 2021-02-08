package io.github.zj.store;

import io.github.zj.factory.MQClientInstance;
import io.github.zj.message.MessageQueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName RemoteBrokerOffsetStore
 * @Description: 集群消费模式下消费进度管理
 * @author: zhangjie
 * @Date: 2021/2/8 11:03
 **/
public class RemoteBrokerOffsetStore implements OffsetStore {

    private final MQClientInstance mQClientFactory;

    private final String groupName;

    public RemoteBrokerOffsetStore(MQClientInstance mQClientFactory, String groupName) {
        this.mQClientFactory = mQClientFactory;
        this.groupName = groupName;
    }

    /** 队列的消费进度映射 */
    private ConcurrentMap<MessageQueue, AtomicLong> offsetTable = new ConcurrentHashMap<MessageQueue, AtomicLong>();

    @Override
    public void removeOffset(MessageQueue mq) {
        if (mq != null) {
            this.offsetTable.remove(mq);
            System.out.println("remove unnecessary messageQueue offset");
        }
    }

    @Override
    public long readOffset(MessageQueue mq, ReadOffsetType type) {
        if (mq != null) {
            switch (type) {
                case READ_FROM_STORE: {
                    long brokerOffset = this.fetchConsumeOffsetFromBroker(mq);
                    AtomicLong offset = new AtomicLong(brokerOffset);
                    this.updateOffset(mq, offset.get());
                    return brokerOffset;
                }
                default:
                    break;
            }
        }
        return -1;
    }

    @Override
    public void updateOffset(MessageQueue mq, long offset) {
        if (mq != null) {
            AtomicLong offsetOld = this.offsetTable.get(mq);
            if (null == offsetOld) {
                offsetOld = this.offsetTable.putIfAbsent(mq, new AtomicLong(offset));
            }
            if (null != offsetOld) {
                offsetOld.set(offset);
            }

        }
    }

    /**
     * 从Mysql中获取队列的消费进度
     */
    private long fetchConsumeOffsetFromBroker(MessageQueue mq){
        Long consumerOffset = this.mQClientFactory.getClientApi().fetchConsumeOffset(this.groupName,mq);
        return consumerOffset == null ? -1 : consumerOffset;
    }
}
