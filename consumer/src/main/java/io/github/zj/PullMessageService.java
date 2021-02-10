package io.github.zj;

import io.github.zj.common.ServiceThread;
import io.github.zj.factory.MQClientInstance;

import java.util.concurrent.*;

/**
 * @ClassName PullMessageService
 * @Description: 拉取队列消息的线程
 * @author: zhangjie
 * @Date: 2021/2/8 14:47
 **/
public class PullMessageService extends ServiceThread {

    private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();

    private final MQClientInstance mQClientFactory;

    private final ScheduledExecutorService scheduledExecutorService = Executors
            .newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "PullMessageServiceScheduledThread");
                }
            });


    public PullMessageService(MQClientInstance mQClientFactory) {
        this.mQClientFactory = mQClientFactory;
    }

    @Override
    public String getServiceName() {
        return PullMessageService.class.getSimpleName();
    }

    @Override
    public void run() {
        System.out.println(String.format("%s service started",getServiceName()));
        while (!this.isStopped()) {
            try{
                PullRequest pullRequest = this.pullRequestQueue.take();
                this.pullMessage(pullRequest);
            }catch (Exception e){
                System.out.println("Pull Message Service Run Method exception "+e);
            }
        }
    }

    /**
     * 拉取队列消息
     */
    private void pullMessage(final PullRequest pullRequest) {
        //获取对应消费组下的消费者实例
        final MQPushConsumer consumer = this.mQClientFactory.selectConsumer(pullRequest.getConsumerGroup());
        if (consumer != null) {
            DefaultMQPushConsumer impl = (DefaultMQPushConsumer) consumer;
            impl.pullMessage(pullRequest);
        }else{
            System.out.println(String.format("No matched consumer for the PullRequest %s, drop it",pullRequest));
        }
    }

    /**
     * 因触发流控，延迟拉取队列消息
     * @param pullRequest
     * @param timeDelay 延迟时间
     */
    public void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {
        if (!isStopped()) {
            this.scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    PullMessageService.this.executePullRequestImmediately(pullRequest);
                }
            },timeDelay, TimeUnit.MILLISECONDS);
        }else{
            System.out.println("PullMessageServiceScheduledThread has shutdown");
        }
    }

    /**
     * 添加队列消息拉取任务
     * @param pullRequest
     */
    public void executePullRequestImmediately(final PullRequest pullRequest) {
        try {
            this.pullRequestQueue.put(pullRequest);
        } catch (InterruptedException e) {
            System.out.println(String.format("executePullRequestImmediately pullRequestQueue.put Exception:%s",e));
        }
    }


}
