package io.github.zj;

import io.github.zj.common.ServiceThread;
import io.github.zj.factory.MQClientInstance;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName PullMessageService
 * @Description: 拉取队列消息的线程
 * @author: zhangjie
 * @Date: 2021/2/8 14:47
 **/
public class PullMessageService extends ServiceThread {

    private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();

    private final MQClientInstance mQClientFactory;

    public PullMessageService(MQClientInstance mQClientFactory) {
        this.mQClientFactory = mQClientFactory;
    }

    @Override
    public String getServiceName() {
        return PullMessageService.class.getSimpleName();
    }

    @Override
    public void run() {

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
