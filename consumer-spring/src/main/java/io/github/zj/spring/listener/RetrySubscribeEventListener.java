package io.github.zj.spring.listener;

import io.github.zj.ConsumeFromWhere;
import io.github.zj.DefaultMQPushConsumer;
import io.github.zj.common.protocol.heartbeat.MessageModel;
import io.github.zj.config.ClientConfig;
import io.github.zj.exception.MQClientException;
import io.github.zj.factory.MQClientInstance;
import io.github.zj.impl.MQClientManager;
import io.github.zj.listener.ConsumeConcurrentlyContext;
import io.github.zj.listener.ConsumeConcurrentlyStatus;
import io.github.zj.listener.MessageListener;
import io.github.zj.message.ConsumerGroupMetadata;
import io.github.zj.message.MessageExt;
import io.github.zj.remote.ClientApi;
import io.github.zj.spring.config.RetryMqContext;
import io.github.zj.spring.config.RetryMqMessageListenerImpl;
import io.github.zj.spring.config.RetryMqSubscribeInfo;
import io.github.zj.spring.enums.ConsumeFrom;
import io.github.zj.spring.utils.IntercepterUtil;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName RetrySubscribeEventListener
 * @Description: 重试订阅监听器(监听容器启动)
 * @author: zhangjie
 * @Date: 2021/2/7 16:26
 **/
public class RetrySubscribeEventListener extends ClientConfig implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware, EnvironmentAware {

   private ApplicationContext applicationContext;

   private Environment environment;

    private DefaultMQPushConsumer consumer;

    private static final Map<String, DefaultMQPushConsumer> consumers = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        //将clientApi注入到MQClientInstance中
        String clientApiName = environment.getProperty("retry.config.client.datasource.className");
        ClientApi clientApi = (ClientApi) applicationContext.getBean(clientApiName);

        //启动消费者监听
        Map<String, RetryMqSubscribeInfo> consumerInfo = RetryMqContext.getConsumerInfo();
        consumerInfo.forEach((key,value) ->{
            RetryMqMessageListenerImpl proxyObj;
            try{
                proxyObj = IntercepterUtil.getProxyObj(RetryMqMessageListenerImpl.class,new Class[]{String.class},new Object[]{key},"RETRY.Consumer");
            }catch (Exception e){
                throw new RuntimeException("加载消息者监听者插件失败",e);
            }

            try {
                doSubscribe(key,proxyObj,clientApi);
            } catch (MQClientException e) {
                System.out.println(e.getErrorMessage());
            }
        });


        changeInstanceNameToPID();
        MQClientInstance mqClientInstance =  MQClientManager.getInstance().getOrCreateMQClientInstance(this);
        mqClientInstance.setClientApi(clientApi);

    }

    /**
     * 1. 根据消费组名称查看配置信息
     * @param consumerGroup
     * @param listener
     */
    private DefaultMQPushConsumer doSubscribe(String consumerGroup,MessageListener listener,ClientApi clientApi) throws MQClientException {
        if (consumers.get(consumerGroup) == null) {
            synchronized (RetrySubscribeEventListener.class){
                if(consumers.get(consumerGroup) == null){
                    ConsumerGroupMetadata metadata = clientApi.readConsumerGroupMetadata(consumerGroup);
                    consumer = new DefaultMQPushConsumer(consumerGroup);
                    String consumeFrom = metadata.getConsumeFrom();
                    if (StringUtils.isEmpty(consumeFrom)) {
                        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
                    } else {
                        if (ConsumeFrom.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
                            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                        } else {
                            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
                        }
                    }
                    String broadCast = metadata.getBroadcast();
                    if (!StringUtils.isEmpty(broadCast) && Boolean.parseBoolean(broadCast)) {
                        consumer.setMessageModel(MessageModel.BROADCASTING);
                    }
                    consumer.subscribe(metadata.getBindingTopic());
                    consumer.setMessageListenerInner(new MessageListener() {
                        @Override
                        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                            listener.consumeMessage(msgs,context);
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                        }
                    });
                    consumer.start();
                    consumers.putIfAbsent(consumerGroup, consumer);
                }
            }
        }
        return consumers.get(consumerGroup);
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
