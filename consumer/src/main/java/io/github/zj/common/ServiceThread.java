package io.github.zj.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @ClassName ServiceThread
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/3 16:04
 **/
public abstract class ServiceThread implements Runnable {

    private Thread thread;

    protected boolean isDaemon = false;

    private final AtomicBoolean started = new AtomicBoolean(false);

    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);

    protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);

    protected volatile boolean stopped = false;

    public abstract String getServiceName();

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        stopped = false;
        this.thread = new Thread(this, getServiceName());
        this.thread.setDaemon(isDaemon);
        this.thread.start();
    }

    protected void waitForRunning(long interval) {
        if (hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }

        //entry to wait
        waitPoint.reset();

        try {
            waitPoint.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted"+ e);
        } finally {
            hasNotified.set(false);
            this.onWaitEnd();
        }

    }

    public boolean isStopped() {
        return stopped;
    }

    public void setDaemon(boolean daemon) {
        isDaemon = daemon;
    }

    protected void onWaitEnd() {
    }
}
