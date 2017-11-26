package com.youthlin.rpc.core.config;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:43
 */
public interface ServiceConfig extends Config {

    int timeout(Method method);

    boolean async(Method method);

}
