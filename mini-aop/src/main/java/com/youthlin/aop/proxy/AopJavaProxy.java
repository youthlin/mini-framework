package com.youthlin.aop.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-27 19:18
 */
public class AopJavaProxy extends AbstractAopProxy implements InvocationHandler {
    private Object proxy;

    public AopJavaProxy(Object original, Class<?>[] itfs) {
        super(original, itfs);
        proxy = Proxy.newProxyInstance(getClass().getClassLoader(), itfs, this);
    }


    @Override
    public Object getProxy() {
        return proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (accept(getProxy(), getOriginal(), method, args)) {
            //return invoke(buildPjp(method, args));
        }
        return method.invoke(getOriginal(), args);
    }
}
