package com.youthlin.rpc.spi;

import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPreScanner;

/**
 * 在容器扫描之前, 对 @Rpc 的字段创建代理, 这样容器扫描后注入字段时就可以注入了.
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:41
 */
public class RpcPreScanner implements IPreScanner {
    @Override
    public void preScan(Context context) {

    }
}
