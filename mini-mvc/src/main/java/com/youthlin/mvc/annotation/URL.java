package com.youthlin.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解修饰 Controller 或 Controller 中的方法
 * 创建： youthlin.chen
 * 时间： 2017-08-13 13:42.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface URL {
    String value() default "";

    Method[] method() default {};

}
