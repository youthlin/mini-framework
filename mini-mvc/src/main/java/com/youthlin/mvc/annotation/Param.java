package com.youthlin.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注解. 简写时，值是参数模名称。
 * 创建： youthlin.chen
 * 时间： 2017-08-13 17:27.
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value() default "";

    String name() default "";

    boolean required() default true;

    String defaultValue() default "";
}
