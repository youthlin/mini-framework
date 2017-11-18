package com.youthlin.mvc.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-18 14:54
 */
public interface ParameterNameDiscoverer {
    String[] getParameterNames(Method method);

    String[] getParameterNames(Constructor constructor);
}
