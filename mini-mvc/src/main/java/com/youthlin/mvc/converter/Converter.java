package com.youthlin.mvc.converter;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-17 23:09
 */
public interface Converter<T> {
    T convert(String from);
}
