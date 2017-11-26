package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.ProviderConfig;
import com.youthlin.rpc.core.config.RegistryConfig;
import com.youthlin.rpc.core.config.SimpleProviderConfig;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 21:58
 */
public class SimpleRegistry implements Registry {
    private RegistryConfig registryConfig;

    @Override
    public void setConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    @Override
    public void register(ProviderConfig providerConfig, Object serviceInstance) {

    }

    @Override
    public void unregister(ProviderConfig providerConfig, Object serviceInstance) {

    }

    @Override
    public ProviderConfig lookup(ConsumerConfig consumerConfig, Class<?> serviceType) {
        return new SimpleProviderConfig() {
            @Override
            public String host() {
                return registryConfig.host();
            }

            @Override
            public int port() {
                return registryConfig.port();
            }
        };
    }
}
