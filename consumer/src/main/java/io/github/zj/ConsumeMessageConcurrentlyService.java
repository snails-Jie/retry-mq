package io.github.zj;

import io.github.zj.common.ThreadFactoryImpl;
import io.github.zj.listener.ConsumeConcurrentlyContext;
import io.github.zj.listener.MessageListener;
import io.github.zj.message.MessageExt;
import io.github.zj.message.MessageQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @ClassName ConsumeMessageConcurrentlyService
 * @Description: 并发消费消息
 * @author: zhangjie
 * @Date: 2021/2/10 16:54
 **/
public class ConsumeMessageConcurrentlyService implements ConsumeMessageService {

    private final DefaultMQPushConsumer defaultMQPushConsumer;

    private final ScheduledExecutorService scheduledExecutorService;

    private final ThreadPoolExecutor consumeExecutor;

    private final BlockingQueue<Runnable> consumeRequestQueue;

    private final MessageListener messageListener;

    public ConsumeMessageConcurrentlyService(DefaultMQPushConsumer defaultMQPushConsumer,MessageListener messageListener) {
        this.messageListener = messageListener;
        this.defaultMQPushConsumer = defaultMQPushConsumer;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("ConsumeMessageScheduledThread_"));
        this.consumeRequestQueue = new LinkedBlockingQueue<Runnable>();
        this.consumeExecutor = new ThreadPoolExecutor(
                this.defaultMQPushConsumer.getConsumeThreadMin(),
                this.defaultMQPushConsumer.getConsumeThreadMax(),
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.consumeRequestQueue,
                new ThreadFactoryImpl("ConsumeMessageThread_"));
    }

    @Override
    public void submitConsumeRequest(List<MessageExt> msgs, ProcessQueue processQueue, MessageQueue messageQueue, boolean dispathToConsume) {
        final int consumeBatchSize = this.defaultMQPushConsumer.getConsumeMessageBatchMaxSize();
        if (msgs.size() <= consumeBatchSize) {
            ConsumeRequest consumeRequest = new ConsumeRequest(msgs, processQueue, messageQueue);
            try {
                this.consumeExecutor.submit(consumeRequest);
            }catch(RejectedExecutionException e){
                this.submitConsumeRequestLater(consumeRequest);
            }
        }else{
            for (int total = 0; total < msgs.size(); ) {
                List<MessageExt> msgThis = new ArrayList<MessageExt>(consumeBatchSize);
                for (int i = 0; i < consumeBatchSize; i++, total++) {
                    if (total < msgs.size()) {
                        msgThis.add(msgs.get(total));
                    }else{
                        break;
                    }
                }
                ConsumeRequest consumeRequest = new ConsumeRequest(msgThis, processQueue, messageQueue);
                try{
                    this.consumeExecutor.submit(consumeRequest);
                }catch (RejectedExecutionException e){
                    for (; total < msgs.size(); total++) {
                        msgThis.add(msgs.get(total));
                    }
                    this.submitConsumeRequestLater(consumeRequest);
                }

            }
        }
    }

    /**
     * 拒绝后延迟提交
     * @param consumeRequest
     */
    private void submitConsumeRequestLater(final ConsumeRequest consumeRequest) {
        this.scheduledExecutorService.schedule(new Runnable() {

            @Override
            public void run() {
                consumeExecutor.submit(consumeRequest);
            }
        }, 5000, TimeUnit.MILLISECONDS);
    }

    class ConsumeRequest implements Runnable {

        private final List<MessageExt> msgs;
        private final ProcessQueue processQueue;
        private final MessageQueue messageQueue;

        public ConsumeRequest(List<MessageExt> msgs, ProcessQueue processQueue, MessageQueue messageQueue) {
            this.msgs = msgs;
            this.processQueue = processQueue;
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            MessageListener listener = ConsumeMessageConcurrentlyService.this.messageListener;
            ConsumeConcurrentlyContext context = new ConsumeConcurrentlyContext(messageQueue);
            if (msgs != null && !msgs.isEmpty()) {
                //调用暴露给用户的方法
                listener.consumeMessage(Collections.unmodifiableList(msgs), context);
            }
        }
    }
}
