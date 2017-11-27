package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.ProxyFactory;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-27 10:08
 */
public abstract class AbstractConsumerConfig extends AbstractConfig implements ConsumerConfig {
    @Override
    public abstract String host();

    @Override
    public abstract int port();

    @Override
    public abstract Class<? extends ProxyFactory> proxy();

    @Override
    public int timeout(Method method) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean async(Method method) {
        return false;
    }

}
