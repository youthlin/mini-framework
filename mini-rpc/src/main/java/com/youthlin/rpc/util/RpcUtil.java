package com.youthlin.rpc.util;

import com.youthlin.rpc.core.config.Config;
import com.youthlin.rpc.core.config.ConsumerConfig;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-27 23:52
 */
public class RpcUtil {
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

}
