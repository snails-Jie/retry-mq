package io.github.zj.spring.utils;

import io.github.zj.spring.plugin.Interceptor;
import io.github.zj.spring.plugin.PluginConfigManager;
import io.github.zj.spring.plugin.PluginDecorator;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 为对象创建调用拦截代理
 * @author zhangjie
 */
public class IntercepterUtil {

    // 暂未使用
    private static final String INTERCEPTOR_FILE_NAME="io.github.zj.plugin.Interceptor";


    /**
     * 返回有拦截器的代理对象(需要通过构造器为成员赋值)
     * @param cla 需要拦截的对象的Class
     * @param argumentTypes 构造参数的类型Class的数组
     * @param arguments 构造参数值
     * @param typePrefix 类型前缀 如（MQ.Producer.CatMQInterceptor） key MQ.Producer 就是类型前缀
     * @param <T> 需要拦截对象的类型
     * @return
     * @throws Exception
     */
    public static <T> T getProxyObj(Class<T> cla, Class[] argumentTypes, Object[] arguments, String typePrefix) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cla);
        enhancer.setCallback(getInterceptor(typePrefix));
        enhancer.setClassLoader(cla.getClassLoader());
        return (T) enhancer.create(argumentTypes, arguments);
    }

    /**
     * 按顺序由小到大组装拦截器链, order越小的越早调用
     * @param typePrefix
     * @return
     * @throws Exception
     */
    private static Interceptor getInterceptor(String typePrefix) throws Exception{
        //按顺序拿到拦截类，order越小的越在后面（越早处理）
        List<PluginDecorator<Class>> interceptorDecorators = PluginConfigManager.getOrderedPluginClasses(INTERCEPTOR_FILE_NAME, typePrefix, true);
        //先初始化一个最内层的拦截器
        Interceptor interceptor = new Interceptor(null);
        if (interceptorDecorators == null || interceptorDecorators.isEmpty()){
            return interceptor;
        }

        for (PluginDecorator pd : interceptorDecorators){
            Class<Interceptor> interceptorClass = (Class<Interceptor>)pd.getPlugin();
            Constructor<Interceptor> constructor = interceptorClass.getConstructor(MethodInterceptor.class);
            //以上一个拦截器为构造函数参数，构造新的拦截器
            Interceptor tmp = constructor.newInstance(interceptor);
            Interceptor tmpInterceptor = new Interceptor(tmp);
            interceptor = tmpInterceptor;
        }

        return interceptor;
    }
}
