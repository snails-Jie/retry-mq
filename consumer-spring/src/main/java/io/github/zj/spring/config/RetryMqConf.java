package io.github.zj.spring.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author zhangjie
 * 重试监听配置
 */
public class RetryMqConf {
    private String consumerGroup;
    /** 标有@RetryListenerParameter注解的参数的属性  */
    private List<Map<String, Object>> params;
    private Object obj;
    private Method method;

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
