package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.Registry;
import com.youthlin.rpc.core.SimpleRegistry;
import com.youthlin.rpc.util.NetUtil;

/**
 * 最简易的注册中心, 即本机担任注册中心的工作, 不支持分布式
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:24
 */
public class SimpleRegistryConfig extends AbstractConfig implements RegistryConfig {
    @Override
    public String name() {
        return NetUtil.LOCALHOST;
    }

    @Override
    public Class<? extends Registry> impl() {
        return SimpleRegistry.class;
    }

    @Override
    public String host() {
        return NetUtil.LOCALHOST_IP;
    }

    @Override
    public int port() {
        return NetUtil.getAvailablePort(NetUtil.DEFAULT_REGISTRY_PORT);
    }

}
