package com.youthlin.ioc.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 14:28.
 */
@Bean
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    String value() default "";
}
