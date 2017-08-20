package com.youthlin.ioc.spi;

import com.youthlin.ioc.context.Context;

/**
 * 在容器扫描之前执行一些动作，如可以先将一部分 Bean 注册到容器中
 * 在你的项目中的 META-INF/services/com.youthlin.ioc.spi.IPreScanner
 * 写上实现类的全限定名可能会被加载(如 mini-mvc)
 * <p>
 * 创建： youthlin.chen
 * 时间： 2017-08-18 19:38.
 */
public interface IPreScanner {
    void preScan(Context context);
}
