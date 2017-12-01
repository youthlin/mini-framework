package com.youthlin.rpc.core.config;

import java.io.Serializable;
import java.util.Map;

/**
 * @see com.youthlin.rpc.util.RpcUtil
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public interface Config extends Serializable {
    /**
     * 临时存一下 并没有发出去 {@link com.youthlin.rpc.core.SimpleProxyFactory}
     */
    String TEMP = "temp";
    /**
     * 方法是否异步 {@link #getConfig() consumerConfig.getConfig(ASYNC, false)}
     */
    String ASYNC = "async";
    /**
     * 是否需要返回值
     */
    String RETURN = "return";
    /**
     * 超时时间
     */
    String TIMEOUT = "timeout";
    /**
     * 参数是否是 callback 参数
     */
    String CALLBACK = "callback";
    /**
     * callback 参数作为 provider 的设置
     */
    String PROVIDER_CONFIG = "providerConfig";
    /**
     * consumer 调用 provider, 传过去的 callback 参数在 provider 端会再次生成一个代理,
     * 这时 provider 端是这个 callback 服务的 consumer, 需要有一个 consumerConfig,
     * 这个 config 是在调用时放在 invocation 里的
     */
    String CONSUMER_CONFIG = "consumerConfig";
    /**
     * callback 参数的超时时间
     */
    String CALLBACK_TIMEOUT = "callbackTimeout";

    Map<String, String> getConfig();

    String getConfig(String key);

    String getConfig(String key, String dft);

    int getConfig(String key, int dft);

    long getConfig(String key, long dft);

    double getConfig(String key, double dft);

    boolean getConfig(String key, boolean dft);

}
