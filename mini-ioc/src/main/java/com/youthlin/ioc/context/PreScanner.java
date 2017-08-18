package com.youthlin.ioc.context;

/**
 * 在容器扫描之前执行一些动作，如可以先将一部分 Bean 注册到容器中
 * 创建： youthlin.chen
 * 时间： 2017-08-18 19:38.
 */
public interface PreScanner {
    void preScan(Context context);
}
