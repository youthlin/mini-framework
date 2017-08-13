package com.youthlin.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注解. 简写时，值是参数名称。
 * 创建： youthlin.chen
 * 时间： 2017-08-13 17:27.
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value() default "";

    String name() default "";

    /**
     * 默认是参数是必须的
     */
    boolean required() default true;

    /**
     * 当参数不是必须的时，可以提供一个默认值
     */
    String defaultValue() default "";
}
