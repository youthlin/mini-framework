package com.youthlin.aop.proxy;

import com.youthlin.aop.core.Advice;
import com.youthlin.aop.core.JoinPointImpl;
import com.youthlin.aop.core.ProceededJoinPoint;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 14:49
 */
public abstract class AbstractAopProxy implements AopProxy {
    private Object advisor;
    private Object original;
    private List<Advice> adviceList = new ArrayList<>();

    AbstractAopProxy(Object advisor, Object original) {
        this.advisor = advisor;
        this.original = original;
    }

    public abstract Object getProxy();

    @Override
    public Object getOriginal() {
        return original;
    }

    public void addAdvice(Advice advice) {
        adviceList.add(advice);
    }

    protected boolean accept(Method method) {
        return false;
    }

    protected ProceededJoinPoint buildPjp(Method method, Object[] args) {
        JoinPointImpl point = new JoinPointImpl();
        point.setTarget(original);
        point.setTargetMethod(method);
        return point;
    }

    public Object invoke(ProceededJoinPoint pjp) {

        return pjp.getResult();
    }

}
