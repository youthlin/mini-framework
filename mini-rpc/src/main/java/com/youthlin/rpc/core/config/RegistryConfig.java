package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.Registry;

/**
 * 注册中心的配置
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public interface RegistryConfig extends Config {
    String name();

    Class<? extends Registry> impl();
}
