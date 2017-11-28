package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.Exporter;
import com.youthlin.rpc.core.SimpleExporter;
import com.youthlin.rpc.util.NetUtil;

import java.lang.reflect.Method;

/**
 * 超时无限长, 同步调用, 默认在 1884 端口暴露服务
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:31
 */
public class SimpleProviderConfig extends AbstractConfig implements ProviderConfig {
    public static final SimpleProviderConfig INSTANCE = new SimpleProviderConfig();
    private String host = NetUtil.ANY_HOST;
    private int port = NetUtil.getAvailablePort(NetUtil.DEFAULT_PORT);

    public SimpleProviderConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public SimpleProviderConfig setPort(int port) {
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
    public Long timeout(Method method) {
        return Long.MAX_VALUE;
    }

    @Override
    public Boolean async(Method method) {
        return false;
    }

    @Override
    public Class<?>[] interfaces() {
        return null;
    }

    @Override
    public Class<? extends Exporter> exporter() {
        return SimpleExporter.class;
    }

}
