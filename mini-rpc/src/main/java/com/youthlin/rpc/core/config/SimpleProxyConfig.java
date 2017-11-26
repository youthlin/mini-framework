package com.youthlin.rpc.core.config;

import com.youthlin.rpc.util.NetUtil;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:35
 */
public class SimpleProxyConfig extends AbstractConfig implements ProxyConfig {
    @Override
    public String host() {
        return NetUtil.getLocalAddress().getHostAddress();
    }

    @Override
    public int port() {
        return NetUtil.getAvailablePort(NetUtil.DEFAULT_PROVIDER_PORT);
    }

}
