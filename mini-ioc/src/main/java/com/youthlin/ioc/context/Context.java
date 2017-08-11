package com.youthlin.ioc.context;

import java.util.Set;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 13:31.
 */
public interface Context {
    Object getBean(String name);

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String name, Class<T> clazz);

    int getBeanCount();

    Set<Object> getBeans();

    Set<String> getUnloadedClass();

}
