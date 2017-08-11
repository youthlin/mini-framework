package com.youthlin.ioc.annotaion;

import java.util.Map;
import java.util.Set;

/**
 * 注解处理器.
 * 创建： youthlin.chen
 * 时间： 2017-08-11 09:48.
 */
public interface IAnnotationProcessor {
    void autoScan(String... scanPackages);

    Set<String> getClassNames();

    Set<String> getUnloadedClass();

    Map<String, Object> getNameBeanMap();

    Map<Class, Object> getClazzBeanMap();

}
