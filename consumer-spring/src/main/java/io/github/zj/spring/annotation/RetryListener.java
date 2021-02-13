package io.github.zj.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RetryListener {
    /**
     *消费组名称
     */
    String consumerGroup();
}
