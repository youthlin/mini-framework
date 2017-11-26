package com.youthlin.rpc.core.config;

import java.util.Map;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public interface Config {
    Map<String, String> getConfig();

    String getConfig(String key);

    String getConfig(String key, String dft);

    Integer getConfig(String key, int dft);

    Long getConfig(String key, long dft);

    Double getConfig(String key, double dft);

    Boolean getConfig(String key, boolean dft);

}
