package io.github.zj.spring.autoconfig;

import io.github.zj.spring.config.ConsumerConfig;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @ClassName EnableMQImportSelector
 * @Description: 动态导入配置类
 * @author: zhangjie
 * @Date: 2021/2/5 22:40
 **/
public class EnableConsumerImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{
                ConsumerConfig.class.getName()
        };
    }
}
