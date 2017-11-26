package com.youthlin.rpc.core.config;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:43
 */
public interface ServiceConfig extends Config {
    /**
     * 提供者的地址
     */
    String host();

    /**
     * 提供者的端口
     */
    int port();

    int timeout(Method method);

    boolean async(Method method);

}
