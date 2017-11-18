package com.youthlin.mvc.support.mybatis;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-19 01:26.
 */
@Resource
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapper {
    String value() default "";
}
