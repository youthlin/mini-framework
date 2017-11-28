package com.youthlin.rpc.core.config;

import java.io.Serializable;
import java.util.Map;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public interface Config extends Serializable{
    String TEMP = "temp";
    String ASYNC = "async";
    String RETURN = "return";
    String TIMEOUT = "timeout";
    String CALLBACK = "callback";
    String PROVIDER_CONFIG = "providerConfig";
    String CONSUMER_CONFIG = "consumerConfig";

    Map<String, String> getConfig();

    String getConfig(String key);

    String getConfig(String key, String dft);

    int getConfig(String key, int dft);

    long getConfig(String key, long dft);

    double getConfig(String key, double dft);

    boolean getConfig(String key, boolean dft);

}
