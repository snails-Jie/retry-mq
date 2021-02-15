package io.github.zj.spring.plugin;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class Interceptor implements MethodInterceptor {
    private MethodInterceptor methodInterceptor;

    public Interceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    /**
     * 如果方法拦截器存在，就调用拦截器的intercept方法，否则直接调用父类的方法
     * @param proxy
     * @param method
     * @param params
     * @param methodProxy
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object proxy, Method method, Object[] params,
                            MethodProxy methodProxy) throws Throwable {
        Object result = null;
        doBefore(proxy, method, params, methodProxy);
        if (methodInterceptor == null) {
            result = methodProxy.invokeSuper(proxy, params);
        } else {
            result = methodInterceptor.intercept(proxy, method, params, methodProxy);
        }
        doAfter(result, proxy, method, params, methodProxy);
        return result;
    }

    protected void doBefore(Object proxy, Method method, Object[] params,
                            MethodProxy methodProxy) {
    }

    protected void doAfter(Object result, Object proxy, Method method, Object[] params,
                           MethodProxy methodProxy) {
    }
}
