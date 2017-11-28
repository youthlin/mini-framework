package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ProviderConfig;

import java.util.concurrent.TimeUnit;

/**
 * 提供者与消费者通信的代理
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:32
 */
public interface Exporter {
    /**
     * @param providerConfig 可指定暴露哪些接口\提供者的端口
     * @param instance       服务提供者实例
     */
    void export(ProviderConfig providerConfig, Object instance);

    void unExport(ProviderConfig providerConfig, Object instance, long delay, TimeUnit unit);

    /**
     * @param invocation 请求需要包含 uid, interfaceType, methodName, argsType[], args[],
     * @return 响应需要包含 uid, value, exception(如果异常)
     */
    Invocation handler(Invocation invocation);

}
