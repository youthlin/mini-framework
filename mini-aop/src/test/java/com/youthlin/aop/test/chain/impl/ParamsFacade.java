package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Params;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 13:09
 */
public class ParamsFacade implements Params {
    private Params delegate;

    public ParamsFacade(Params delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getThis() {
        return delegate.getThis();
    }

    @Override
    public Object getTarget() {
        return delegate.getTarget();
    }

    @Override
    public Object[] getArgs() {
        return delegate.getArgs();
    }

    @Override
    public Object proceed() throws Throwable {
        return delegate.proceed();
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return delegate.proceed(args);
    }

    @Override
    public Object getReturn() {
        return delegate.getReturn();
    }

    @Override
    public Throwable getThrowable() {
        return delegate.getThrowable();
    }
}
