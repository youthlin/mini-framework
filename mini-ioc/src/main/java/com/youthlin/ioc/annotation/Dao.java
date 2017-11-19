package com.youthlin.ioc.annotation;

import javax.annotation.Resource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 14:52.
 */
@Resource
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Dao {
    String value() default "";
}
