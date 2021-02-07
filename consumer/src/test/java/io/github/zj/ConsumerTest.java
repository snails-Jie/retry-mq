package io.github.zj;

import io.github.zj.exception.MQClientException;

/**
 * @ClassName ConsumerTest
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/3 22:01
 **/
public class ConsumerTest {
    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test");
        consumer.subscribe("TopicTest");
        consumer.start();
    }
}
