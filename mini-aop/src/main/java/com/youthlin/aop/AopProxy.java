package com.youthlin.aop;

/**
 * 创建: youthlin.chen
 * 时间: 2017-12-26 19:52
 */
public interface AopProxy {
    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
