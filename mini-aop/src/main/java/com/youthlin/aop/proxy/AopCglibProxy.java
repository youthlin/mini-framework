package com.youthlin.aop.proxy;

import com.youthlin.aop.advice.AbstractAdvice;
import com.youthlin.aop.core.Invocation;
import com.youthlin.aop.core.JoinPointImpl;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-27 19:18
 */
public class AopCglibProxy extends AbstractAopProxy implements MethodInterceptor {
    private Object proxy;

    public AopCglibProxy(Object original, Class<?>[] itfs) {
        super(original, itfs);
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
        List<AbstractAdvice> matchedAdviceList = getMatchedAdviceList(method, args);
        if (!matchedAdviceList.isEmpty()) {
            Invocation invocation = buildInvocation(method, args, matchedAdviceList);
            JoinPointImpl joinPoint = buildPjp(args, matchedAdviceList);
            return invocation.invoke(joinPoint);
        }
        return methodProxy.invoke(getOriginal(), args);
    }
}
