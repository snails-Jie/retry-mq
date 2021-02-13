package io.github.zj.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TitansPlugin {
    /**
     * 插件执行顺序，可为负值，数值越小，越早执行
     * @return
     */
    int order() default 0;
}
