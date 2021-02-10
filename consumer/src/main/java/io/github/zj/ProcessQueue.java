package io.github.zj;

import io.github.zj.message.MessageExt;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName ProcessQueue
 * @Description: 队列消费快照
 * @author: zhangjie
 * @Date: 2021/2/8 10:37
 **/
public class ProcessQueue {

    /** 上次拉取时间 */
    private volatile long lastPullTimestamp = System.currentTimeMillis();

    /** 消息数量 */
    private final AtomicLong msgCount = new AtomicLong();

    /** 消息大小 */
    private final AtomicLong msgSize = new AtomicLong();

    private final ReadWriteLock lockTreeMap = new ReentrantReadWriteLock();
    /**
     * 成功回调：PullCallback#onSuccess -> ProcessQueue#putMessage
     */
    private final TreeMap<Long, MessageExt> msgTreeMap = new TreeMap<Long, MessageExt>();

    private volatile boolean consuming = false;


    public boolean putMessage(final List<MessageExt> msgs) {
        boolean dispatchToConsume = false;
        try {
            this.lockTreeMap.writeLock().lockInterruptibly();
            try {
                int validMsgCnt = 0;
                for (MessageExt msg : msgs) {
                    MessageExt old = msgTreeMap.put(msg.getQueueOffset(), msg);
                    if (null == old) {
                        validMsgCnt++;
                        // 暂时忽略，因为不是返回字节数组
//                        msgSize.addAndGet(msg.getBody().length);
                    }
                }
                msgCount.addAndGet(validMsgCnt);
                if (!msgTreeMap.isEmpty() && !this.consuming) {
                    dispatchToConsume = true;
                    this.consuming = true;
                }
                //msgAccCnt 监控调整线程池大小（暂未实现）
            }finally {
                this.lockTreeMap.writeLock().unlock();
            }
        }catch (InterruptedException e){
            System.out.println("putMessage exception:"+e);
        }

        return dispatchToConsume;
    }


        /**
         * 获取最大跨度
         * @return
         */
    public long getMaxSpan(){
        try{
            this.lockTreeMap.readLock().lockInterruptibly();
            try{
                if(!this.msgTreeMap.isEmpty()){
                    return this.msgTreeMap.lastKey() - this.msgTreeMap.firstKey();
                }
            }finally {
                this.lockTreeMap.readLock().unlock();
            }
        }catch (InterruptedException e){
            System.out.println("getMaxSpan exception:"+e);
        }

        return 0;
    }


    public long getLastPullTimestamp() {
        return lastPullTimestamp;
    }

    public void setLastPullTimestamp(long lastPullTimestamp) {
        this.lastPullTimestamp = lastPullTimestamp;
    }

    public AtomicLong getMsgCount() {
        return msgCount;
    }

    public AtomicLong getMsgSize() {
        return msgSize;
    }
}
