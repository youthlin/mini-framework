package com.youthlin.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示返回值需要直接输出至浏览器.
 * 只用于修饰 Controller 中的方法，或自定义注解
 * 创建：youthlin.chen
 * 时间：2017-08-13 23:29
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseBody {
}
