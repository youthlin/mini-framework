package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.ProviderConfig;

/**
 * 注册中心
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:53
 */
public interface Registry {
    void register(ProviderConfig providerConfig, Object serviceInstance);

    void unregister(ProviderConfig providerConfig, Object serviceInstance);

    ProviderConfig lookup(ConsumerConfig consumerConfig, Class<?> serviceType);

}
