package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Chain;
import com.youthlin.aop.test.chain.Handler;
import com.youthlin.aop.test.chain.Params;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 13:17
 */
public class HandlerAdapter implements Handler {
    @Override
    public void handle(Params params, Chain chain) throws Throwable {
        before(params);
        try {
            doHandle(params, chain);
            after(params);
        } catch (final Throwable t) {
            params = new ParamsFacade(params) {
                @Override
                public Throwable getThrowable() {
                    return t;
                }
            };
            onException(params);
        } finally {
            onDone(params);
        }
    }

    protected void before(Params params) {
    }

    protected void after(Params params) {
    }

    protected void doHandle(Params params, Chain chain) throws Throwable {
        params.proceed();
    }

    protected void onException(Params params) throws Throwable {
        throw params.getThrowable();
    }

    protected void onDone(Params params) {
    }
}
