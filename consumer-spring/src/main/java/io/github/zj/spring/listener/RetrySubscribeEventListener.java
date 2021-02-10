package io.github.zj.spring.listener;

import io.github.zj.ConsumeFromWhere;
import io.github.zj.DefaultMQPushConsumer;
import io.github.zj.config.ClientConfig;
import io.github.zj.exception.MQClientException;
import io.github.zj.factory.MQClientInstance;
import io.github.zj.impl.MQClientManager;
import io.github.zj.listener.ConsumeConcurrentlyContext;
import io.github.zj.listener.ConsumeConcurrentlyStatus;
import io.github.zj.listener.MessageListener;
import io.github.zj.message.MessageExt;
import io.github.zj.remote.ClientApi;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * @ClassName RetrySubscribeEventListener
 * @Description: 重试订阅监听器(监听容器启动)
 * @author: zhangjie
 * @Date: 2021/2/7 16:26
 **/
public class RetrySubscribeEventListener extends ClientConfig implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware, EnvironmentAware {

   private ApplicationContext applicationContext;

   private Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try {
            //启动消费者
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test");
            consumer.subscribe("TopicTest");
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setMessageListenerInner(new MessageListener() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    System.out.println(msgs.size());
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();

            //将clientApi注入到MQClientInstance中
            String clientApiName = environment.getProperty("retry.config.client.datasource.className");
            ClientApi clientApi = (ClientApi) applicationContext.getBean(clientApiName);
            changeInstanceNameToPID();
            MQClientInstance mqClientInstance =  MQClientManager.getInstance().getOrCreateMQClientInstance(this);
            mqClientInstance.setClientApi(clientApi);

        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
