package io.github.zj;

import io.github.zj.common.ServiceThread;
import io.github.zj.factory.MQClientInstance;

/**
 * @ClassName RebalanceService
 * @Description: 队列负载线程
 * @author: zhangjie
 * @Date: 2021/2/3 16:08
 **/
public class RebalanceService extends ServiceThread {

    private static long waitInterval = Long.parseLong(System.getProperty("rocketmq.client.rebalance.waitInterval", "20000"));

    private final MQClientInstance mqClientFactory;

    public RebalanceService(MQClientInstance mqClientFactory) {
        this.mqClientFactory = mqClientFactory;
    }

    @Override
    public String getServiceName() {
        return RebalanceService.class.getSimpleName();
    }

    public void run() {
        while (!this.isStopped()) {
            this.waitForRunning(waitInterval);
            this.mqClientFactory.doRebalance();
        }
    }
}
