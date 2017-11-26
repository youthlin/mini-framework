package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.Exporter;
import com.youthlin.rpc.core.ProxyFactory;
import com.youthlin.rpc.core.SimpleProxyFactory;
import com.youthlin.rpc.util.NetUtil;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:31
 */
public class SimpleConsumerConfig extends AbstractConfig implements ConsumerConfig {
    @Override
    public String host() {
        return NetUtil.getLocalAddress().getHostAddress();
    }

    @Override
    public int port() {
        return NetUtil.getAvailablePort(NetUtil.DEFAULT_PROVIDER_PORT);
    }

    @Override
    public int timeout(Method method) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean async(Method method) {
        return false;
    }

    @Override
    public Class<? extends ProxyFactory> impl() {
        return SimpleProxyFactory.class;
    }
}
