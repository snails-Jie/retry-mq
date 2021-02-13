package io.github.zj.spring.config;

import io.github.zj.listener.ConsumeConcurrentlyContext;
import io.github.zj.listener.ConsumeConcurrentlyStatus;
import io.github.zj.listener.MessageListener;
import io.github.zj.message.MessageExt;
import io.github.zj.spring.constants.MqConsts;
import io.github.zj.spring.enums.MQMsgEnum;

import java.util.List;
import java.util.Map;

/**
 * 消息监听器
 */
public class RetryMqMessageListenerImpl implements MessageListener {
    private String consumerGroup;

    public RetryMqMessageListenerImpl(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try{
            for(MessageExt msg : msgs){
                RetryMqConf retryMqConf = RetryMqContext.getRetryConfMap().get(consumerGroup);
                //参数集合（每个参数都带有@RetryListenerParameter注解）
                String[] params = new String[retryMqConf.getParams().size()];
                for (int i = 0; i < retryMqConf.getParams().size(); i++) {
                    //参数的注解属性
                    Map<String, Object> map = retryMqConf.getParams().get(i);
                    String param = null;
                    if(map.get(MqConsts.RetryMqListener.NAME).toString().equalsIgnoreCase(MQMsgEnum.BODY.getValue())){
                        param = msg.getContent();
                    }
                    params[i] = param;
                }
                retryMqConf.getMethod().invoke(retryMqConf.getObj(), params);
            }
        }catch (Exception e){
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;


    }
}
