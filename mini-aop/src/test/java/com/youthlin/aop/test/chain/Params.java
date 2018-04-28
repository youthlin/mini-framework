package com.youthlin.aop.test.chain;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 12:15
 */
public interface Params {
    Object getThis();

    Object getTarget();

    Object[] getArgs();

    Object proceed() throws Throwable;

    Object proceed(Object[] args) throws Throwable;

    Object getReturn();

    Throwable getThrowable();
}
