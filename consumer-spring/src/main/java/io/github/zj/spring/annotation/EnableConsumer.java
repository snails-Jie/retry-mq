package io.github.zj.spring.annotation;

import io.github.zj.spring.autoconfig.EnableConsumerImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启基于MYSQL-MQ消费端
 * @author zhangjie
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableConsumerImportSelector.class})
public @interface EnableConsumer {
}
