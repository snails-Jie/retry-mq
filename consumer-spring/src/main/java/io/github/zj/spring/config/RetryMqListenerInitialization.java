package io.github.zj.spring.config;

import io.github.zj.spring.annotation.RetryListener;
import io.github.zj.spring.annotation.RetryListenerParameter;
import io.github.zj.spring.constants.MqConsts;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 消息监听的初始化类:扫描监听注解的方法
 */
public class RetryMqListenerInitialization implements BeanPostProcessor, PriorityOrdered {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //获取bean的所有方法
        Method[] methods = bean.getClass().getMethods();

        Arrays.stream(methods).forEach(method->{
            if(method.isAnnotationPresent(RetryListener.class)){
                //获取监听方法的参数
                Parameter[] parameters = method.getParameters();
                if (parameters.length <= 0) {
                    throw new RuntimeException(String.format("ZMSListener方法%s入参为空", bean.getClass().getName() + "." + method.getName()));
                }
                List<Map<String, Object>> paramList = new ArrayList<>();
                Arrays.stream(parameters).forEach(parameter -> {
                    RetryListenerParameter param = parameter.getAnnotation(RetryListenerParameter.class);
                    Map<String, Object> map = new HashMap<>();
                    if(param != null){
                        map.put(MqConsts.RetryMqListener.NAME,param.name());
                        map.put(MqConsts.RetryMqListener.SERIALIZE,param.serialize().getValue());
                        paramList.add(map);
                    }
                });


                RetryListener annotation = method.getAnnotation(RetryListener.class);
                String consumerGroup = annotation.consumerGroup();

                RetryMqSubscribeInfo retryMqSubscribeInfo = new RetryMqSubscribeInfo();
                RetryMqContext.putConsumerInfo(consumerGroup,retryMqSubscribeInfo);


                RetryMqContext.getRetryConfMap().put(consumerGroup,
                        RetryMqContext.getRetryMqConf(consumerGroup,method,bean,paramList));

            }
        });

        return bean;
    }

    /**
     * 防止被其他代理导致失效，RetryMqListenerInitialization最先执行
     * @return
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
