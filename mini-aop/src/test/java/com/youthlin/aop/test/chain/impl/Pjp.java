package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Chain;
import com.youthlin.aop.test.chain.Params;

import java.util.Arrays;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 13:11
 */
public class Pjp implements Params {
    private Object thisObject;
    private Object targetObject;
    private Object[] args;
    private boolean proceed;
    private Object returnObject;
    private Throwable throwable;
    public Chain chain;

    @Override
    public Object getThis() {
        return thisObject;
    }

    @Override
    public Object getTarget() {
        return targetObject;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public Object proceed() throws Throwable {
        return proceed(args);
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
//        if (proceed) {
//            throw new IllegalStateException("already proceed");
//        }
        proceed = true;
        chain.doHandle(this);
        return returnObject;
    }

    @Override
    public Object getReturn() {
        return returnObject;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "Pjp{" +
                "thisObject=" + thisObject +
                ", targetObject=" + targetObject +
                ", args=" + Arrays.toString(args) +
                ", proceed=" + proceed +
                ", returnObject=" + returnObject +
                ", throwable=" + getThrowable() +
                ", chain=" + chain +
                '}';
    }
}
