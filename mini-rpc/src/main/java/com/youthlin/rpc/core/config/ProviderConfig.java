package com.youthlin.rpc.core.config;

import com.youthlin.rpc.core.Exporter;

/**
 * 提供者配置
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public interface ProviderConfig extends ServiceConfig {
    /**
     * 可以指定暴露的接口, 若为 null 则取其所有已实现的接口
     */
    Class<?>[] interfaces();

    Class<? extends Exporter> exporter();
}
