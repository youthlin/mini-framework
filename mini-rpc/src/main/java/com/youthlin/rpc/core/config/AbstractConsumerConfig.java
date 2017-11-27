package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.ProxyFactory;

import java.lang.reflect.Method;

/**
 * 假装提供者在本机,如果不是，请至少覆盖 host, port 方法
 * <p>
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
    public Long timeout(Method method) {
        return Long.MAX_VALUE;
    }

    @Override
    public Boolean async(Method method) {
        return null;
    }

}
