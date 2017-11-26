package com.youthlin.rpc.core;

/**
 * 一次调用过程的信息
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:41
 */
public interface Invocation {
    String uid();

    //请求-----------------------------

    Class<?> invokeInterface();

    Class<?> returnType();

    String methodName();

    Class<?>[] argsType();

    Object[] args();

    //响应-----------------------------

    Object getValue();

    Throwable getException();

}
