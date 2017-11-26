package com.youthlin.rpc.spi;

import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPostScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在容器扫描完成后, 对 @Rpc 的类注册到注册中心.
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:39
 */
public class RpcPostScanner implements IPostScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcPostScanner.class);

    @Override
    public void postScanner(Context context) {

    }
}
