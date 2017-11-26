package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.ProxyFactory;

/**
 * 每个服务的配置
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public interface ConsumerConfig extends ServiceConfig {
    Class<? extends ProxyFactory> proxy();
}
