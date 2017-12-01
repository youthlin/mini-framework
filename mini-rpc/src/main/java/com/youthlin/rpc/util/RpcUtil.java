package com.youthlin.rpc.util;

import com.youthlin.rpc.core.Invocation;
import com.youthlin.rpc.core.config.Config;
import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.ProviderConfig;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-27 23:52
 */
public class RpcUtil {
    public static final Object[] EMPTY_ARRAY = {};

    public static long getTimeOut(ConsumerConfig consumerConfig, Method method) {
        Long timeout = consumerConfig.timeout(method);
        if (timeout == null) {
            timeout = consumerConfig.getConfig(method, Config.TIMEOUT, Long.MAX_VALUE);
            if (timeout == null) {
                timeout = consumerConfig.getConfig(Config.TIMEOUT, Long.MAX_VALUE);
            }
        }
        return timeout;
    }

    public static boolean async(ConsumerConfig consumerConfig, Method method) {
        Boolean async = consumerConfig.async(method);
        if (async == null) {
            async = consumerConfig.getConfig(method, Config.ASYNC, false);
            if (async == null) {
                async = consumerConfig.getConfig(Config.ASYNC, false);
            }
        }
        return async;
    }

    public static boolean needReturn(ConsumerConfig consumerConfig, Method method) {
        Boolean needRet = consumerConfig.getConfig(method, Config.RETURN, true);
        if (needRet == null) {
            needRet = consumerConfig.getConfig(Config.RETURN, true);
        }
        return needRet;
    }

    public static boolean needReturn(Invocation invocation) {
        if (invocation == null) {
            return true;
        }
        if (invocation.ext() == null) {
            return false;
        }
        Serializable needReturn = invocation.ext().get(Config.RETURN);
        return needReturn == null || needReturn instanceof Boolean && (boolean) needReturn;
    }

    /**
     * 指定某个方法的那几个参数是 callback 参数
     */
    public static boolean[] callback(ConsumerConfig consumerConfig, Method method) {
        int length = method.getParameterTypes().length;
        boolean[] config = consumerConfig.getConfig(method, Config.CALLBACK, (boolean[]) null);
        if (config != null) {
            if (config.length == length) {
                return config;
            }
            throw new IllegalArgumentException("CallBack config should return boolean[], which length should be parameters size. " + method);
        }
        return null;
    }

    public static ProviderConfig[] providerConfigOfCallbackParameter(ConsumerConfig consumerConfig, Method method) {
        ProviderConfig[] providerConfigs = consumerConfig.getConfig(method, Config.PROVIDER_CONFIG, (ProviderConfig[]) null);
        if (providerConfigs != null) {
            if (providerConfigs.length == method.getParameterTypes().length) {
                return providerConfigs;
            }
            throw new IllegalArgumentException("ProviderConfig of callBack parameter should return ProviderConfig[], which length should be parameters size. " + method);
        }
        return null;
    }

}
