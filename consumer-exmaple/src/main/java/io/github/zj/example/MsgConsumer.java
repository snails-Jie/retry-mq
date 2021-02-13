package io.github.zj.example;

import io.github.zj.spring.annotation.RetryListener;
import io.github.zj.spring.annotation.RetryListenerParameter;
import io.github.zj.spring.enums.MQMsgEnum;
import org.springframework.stereotype.Component;

@Component
public class MsgConsumer {

    @RetryListener(consumerGroup = "test")
    public void onMessage(@RetryListenerParameter(name = MQMsgEnum.BODY) String body){
        System.out.println("收到消息："+body);
    }

}
