package com.youthlin.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于修饰 Controller 方法中的参数, 表示从 Request 的 Body 中直接读取
 * 通常应该配合 @{@link ConvertWith} 使用
 * 创建: youthlin.chen
 * 时间: 2017-11-19 12:58
 */
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
}
