package io.github.zj.spring.utils;

import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @ClassName PropertyBinder
 * @Description: 读取配置
 * @author: zhangjie
 * @Date: 2021/2/6 11:10
 **/
public class PropertyBinder {
    private ConfigurableEnvironment environment;
    private Binder binder;

    public PropertyBinder(ConfigurableEnvironment environment) {
        this.environment = environment;
        this.binder = new Binder(this.getConfigurationPropertySources(), this.getPropertySourcesPlaceholdersResolver());
    }

    /**
     * 将指定配置前缀的所有配置读取到配置类中
     * @param configPrefix 配置前缀
     * @param target 指定配置类
     * @param <T> 返回参数
     * @return
     */
    public <T> BindResult<T> bind(String configPrefix, Class<T> target) {
        return this.binder.bind(configPrefix, Bindable.of(target));
    }

    private Iterable<ConfigurationPropertySource> getConfigurationPropertySources() {
        return ConfigurationPropertySources.from(this.environment.getPropertySources());
    }

    private PropertySourcesPlaceholdersResolver getPropertySourcesPlaceholdersResolver() {
        return new PropertySourcesPlaceholdersResolver(this.environment.getPropertySources());
    }
}
