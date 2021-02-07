package io.github.zj.spring.config;

import io.github.zj.config.ClientConfig;
import io.github.zj.factory.MQClientInstance;
import io.github.zj.impl.MQClientManager;
import io.github.zj.remote.ClientApi;
import io.github.zj.remote.ClientApiManager;
import io.github.zj.spring.listener.RetrySubscribeEventListener;
import io.github.zj.spring.utils.PropertyBinder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Objects;

/**
 * BeanDefinitionRegistryPostProcessor动态注入BeanDefinition(根据配置文件)
 * EnvironmentAware:读取指定环境的配置
 * @ClassName ConsumerConfig
 * @Description: 消费者配置
 * @author: zhangjie
 * @Date: 2021/2/5 17:45
 **/
@MapperScan({"io.github.zj.spring.dao"})
public class ConsumerConfig extends ClientConfig implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private final String dataSourceTypeStr = "retry.config.client.datasource.type";
    private final String mysql = "mysql";

    protected ConfigurableEnvironment env;



    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        if (this.env.getProperty(dataSourceTypeStr) != null) {
            ConsumerProperties consumerProperties = loadSingleConfig(this.env,ConsumerProperties.class);
            if(consumerProperties == null){
               return;
            }
            registerBeans(beanFactory);
            registerClientApi(consumerProperties,beanFactory);
        }
    }

    /** 注册clientApi */
    private void registerClientApi(ConsumerProperties consumerProperties,BeanDefinitionRegistry beanFactory){
        if(!consumerProperties.getType().equals(mysql)){
            return;
        }
        List<ClientApi> clientApiList = ClientApiManager.getClientApis();
        for(ClientApi clientApi : clientApiList){
            if(clientApi.getClass().getName().equals(consumerProperties.getClassName())){
                //注册clientApi到spring容器中
                BeanDefinitionBuilder dataSourceDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clientApi.getClass());
                beanFactory.registerBeanDefinition(clientApi.getClass().getSimpleName(),dataSourceDefinitionBuilder.getRawBeanDefinition());

                //将clientApi注入到MQClientInstance中
                String clientId = buildMQClientId();
                MQClientInstance mqClientInstance =  MQClientManager.getInstance().getFactoryTable().get(clientId);
                mqClientInstance.setClientApi(clientApi);
                break;
            }
        }
    }



    private <T> T loadSingleConfig(Environment env, Class<T> target){
        PropertyBinder propertyBinder = new PropertyBinder((ConfigurableEnvironment)env);
        BindResult<String> dataSourceType = propertyBinder.bind(dataSourceTypeStr, String.class);
        if(Objects.isNull(dataSourceType)){
            return null;
        }else{
            BindResult<T> bindResult = Binder.get(env).bind("retry.config.client.datasource", target);
            return bindResult.orElse(null);
        }
    }

    /**
     * 1. 注册重试订阅器
     * @param beanFactory
     */
    public void registerBeans(BeanDefinitionRegistry beanFactory) {
        BeanDefinitionBuilder retrySubscribeEventListenerDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RetrySubscribeEventListener.class);
        beanFactory.registerBeanDefinition(RetrySubscribeEventListener.class.getSimpleName(),retrySubscribeEventListenerDefinitionBuilder.getBeanDefinition());
    }

    /** 暂时忽略 */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }
}
