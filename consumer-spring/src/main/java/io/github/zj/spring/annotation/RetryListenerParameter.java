package io.github.zj.spring.annotation;

import io.github.zj.spring.enums.MQMsgEnum;
import io.github.zj.spring.enums.Serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryListenerParameter {

    /**
     * 接收参数名称，目前仅支持 MQMsgEnum 枚举中的参数
     * @return
     */
    MQMsgEnum name();

    Serialize serialize() default Serialize.STRING;
}
