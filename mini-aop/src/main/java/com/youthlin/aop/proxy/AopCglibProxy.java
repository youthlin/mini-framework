package com.youthlin.aop.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-27 19:18
 */
public class AopCglibProxy extends AbstractAopProxy implements MethodInterceptor {
    private Object proxy;
    private Class<?>[] itfs;

    public AopCglibProxy(Object advisor,Object original, Class<?>[] itfs) {
        super(advisor,original);
        this.itfs = itfs;
    }

    @Override
    public Object getProxy() {
        if (proxy == null) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(getOriginal().getClass());
            enhancer.setInterfaces(itfs);
            enhancer.setCallback(this);
            proxy = enhancer.create();
        }
        return proxy;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (accept(method)) {
            return invoke(buildPjp(method, args));
        }
        return methodProxy.invoke(getOriginal(), args);
    }
}
