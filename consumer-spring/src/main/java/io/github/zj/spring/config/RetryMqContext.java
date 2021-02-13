package io.github.zj.spring.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重试队列上下文
 * @author zhangjie
 */
public class RetryMqContext {

    private static Map<String, RetryMqSubscribeInfo> consumerInfo = new ConcurrentHashMap<>();

    private static Map<String,RetryMqConf> retryConfMap = new ConcurrentHashMap();


    public static RetryMqConf getRetryMqConf(String consumerGroup, Method method, Object obj, List<Map<String, Object>> params) {
        RetryMqConf retryMqConf = new RetryMqConf();
        retryMqConf.setConsumerGroup(consumerGroup);
        retryMqConf.setMethod(method);
        retryMqConf.setObj(obj);
        retryMqConf.setParams(params);
        return retryMqConf;
    }

    public static void putConsumerInfo(String consumerGroup, RetryMqSubscribeInfo info) {
        if(!consumerInfo.containsKey(consumerGroup)){
            consumerInfo.put(consumerGroup,info);
        }
    }

    public static Map<String, RetryMqSubscribeInfo> getConsumerInfo() {
        return consumerInfo;
    }


    public static Map<String, RetryMqConf> getRetryConfMap() {
        return retryConfMap;
    }

    public static void setRetryConfMap(Map<String, RetryMqConf> retryConfMap) {
        RetryMqContext.retryConfMap = retryConfMap;
    }


}
