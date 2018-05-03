package com.youthlin.aop.proxy;

import com.youthlin.aop.advice.AbstractAdvice;
import com.youthlin.aop.core.Invocation;
import com.youthlin.aop.core.JoinPointImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

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
        List<AbstractAdvice> matchedAdviceList = getMatchedAdviceList(method, args);
        if (!matchedAdviceList.isEmpty()) {
            Invocation invocation = buildInvocation(method, args, matchedAdviceList);
            JoinPointImpl joinPoint = buildPjp(args, matchedAdviceList);
            return invocation.invoke(joinPoint);
        }
        return method.invoke(getOriginal(), args);
    }

}
