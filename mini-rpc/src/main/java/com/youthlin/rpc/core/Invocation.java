package com.youthlin.rpc.core;

import java.io.Serializable;
import java.util.Map;

/**
 * 一次调用过程的信息
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:41
 */
public interface Invocation extends Serializable {
    String uid();

    Map<String, Serializable> ext();

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
