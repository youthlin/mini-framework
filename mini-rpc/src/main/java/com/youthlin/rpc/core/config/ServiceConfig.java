package com.youthlin.rpc.core.config;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:43
 */
public interface ServiceConfig extends Config {
    /**
     * 提供者的地址
     */
    String host();

    /**
     * 提供者的端口
     */
    int port();

    //region 顺序: async(method) -> getConfig(method, async, dft) -> getConfig(async, dft) 用第一个非 null 的
    Integer timeout(Method method);

    Boolean async(Method method);
    //endregion

    //region 如果返回 null 则使用 Config.getConfig(key, dft) 的配置
    String getConfig(Method method, String key);

    String getConfig(Method method, String key, String dft);

    Integer getConfig(Method method, String key, int dft);

    Long getConfig(Method method, String key, long dft);

    Double getConfig(Method method, String key, double dft);

    Boolean getConfig(Method method, String key, boolean dft);
    //endregion
}
