package com.youthlin.ioc.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定 test 时扫描的包路径, 默认扫描测试类所在的包
 * <p>
 * 创建: youthlin.chen
 * 时间: 2017-12-05 23:48
 *
 * @see MiniRunner
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scan {
    String[] value();
}
