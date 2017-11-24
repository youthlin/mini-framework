package com.youthlin.ioc.spi;

import com.youthlin.ioc.context.Context;

/**
 * 在容器扫描之后执行一些动作，如可以对其中一部分 Bean 进行后续操作
 * 在你的项目中的 META-INF/services/com.youthlin.ioc.spi.IPostScanner
 * 写上实现类的全限定名可能会被加载(如 mini-rpc)
 * <p>
 * 创建: youthlin.chen
 * 时间: 2017-11-24 22:13
 */
public interface IPostScanner {
    void postScanner(Context context);
}
