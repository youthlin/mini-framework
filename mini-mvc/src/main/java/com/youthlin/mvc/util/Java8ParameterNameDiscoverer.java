package com.youthlin.mvc.util;

import com.youthlin.mvc.annotation.Java8;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-18 14:55
 */
@Java8
@Resource
public class Java8ParameterNameDiscoverer implements ParameterNameDiscoverer {
    @Override
    public String[] getParameterNames(Method method) {
        return getParameterNames0(method);
    }

    @Override
    public String[] getParameterNames(Constructor ctor) {
        return getParameterNames0(ctor);
    }

    private String[] getParameterNames0(Executable executable) {
        Parameter[] parameters = executable.getParameters();
        String[] parameterNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            parameterNames[i] = param.getName();
        }
        return parameterNames;
    }
}
