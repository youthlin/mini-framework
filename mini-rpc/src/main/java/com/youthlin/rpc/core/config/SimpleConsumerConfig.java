package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.ProxyFactory;
import com.youthlin.rpc.core.SimpleProxyFactory;
import com.youthlin.rpc.util.NetUtil;

import java.lang.reflect.Method;

/**
 * 假装提供者在本机,如果不是，请至少覆盖 host, port 方法(可继承 {@link AbstractConsumerConfig})
 * <p>
 * 超时无限长, 全部是同步调用, JDK 动态代理
 * <p>
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:31
 */
public class SimpleConsumerConfig extends AbstractConsumerConfig implements ConsumerConfig {
    public static final SimpleConsumerConfig INSTANCE = new SimpleConsumerConfig();
    private String host = NetUtil.LOCALHOST;
    private int port = NetUtil.DEFAULT_PORT;

    public SimpleConsumerConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public SimpleConsumerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public Class<? extends ProxyFactory> proxy() {
        return SimpleProxyFactory.class;
    }

    @Override
    public Long timeout(Method method) {
        return Long.MAX_VALUE;
    }

    @Override
    public Boolean async(Method method) {
        return false;
    }

}
