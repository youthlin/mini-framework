package com.youthlin.mvc.annotation;

import com.youthlin.mvc.converter.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-17 23:06
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConvertWith {
    Class<? extends Converter> value();
}
