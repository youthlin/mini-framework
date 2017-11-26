package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.RegistryConfig;

import java.lang.reflect.InvocationHandler;

/**
 * 创建 @Rpc 的代理, 所有调用远程接口的方法都在这里进行拦截.
 * <p>
 * 拿到方法和参数后, 从注册中心选一个提供者, 把方法\参数包装一下发给提供者, 然后等待结果返回
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:44
 */
public interface ProxyFactory extends InvocationHandler {
    <T> T newProxy(Class<T> interfaceType, RegistryConfig registryConfig, ConsumerConfig consumerConfig);
}
