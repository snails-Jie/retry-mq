package io.github.zj.spring.listener;

import io.github.zj.DefaultMQPushConsumer;
import io.github.zj.exception.MQClientException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * @ClassName RetrySubscribeEventListener
 * @Description: 重试订阅监听器(监听容器启动)
 * @author: zhangjie
 * @Date: 2021/2/7 16:26
 **/
public class RetrySubscribeEventListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try {
            //启动消费者
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test");
            consumer.subscribe("TopicTest");
            consumer.start();

        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


}
