package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ConsumerConfig;

/**
 * 创建 @Rpc 的代理, 所有调用远程接口的方法都在这里进行拦截.
 * <p>
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:44
 */
public interface ProxyFactory {
    <T> T newProxy(Class<T> interfaceType, ConsumerConfig consumerConfig);
}
