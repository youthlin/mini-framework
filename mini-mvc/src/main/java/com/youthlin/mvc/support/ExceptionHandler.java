package com.youthlin.mvc.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-14 16:23.
 */
public interface ExceptionHandler extends Order {
    void handler(Throwable t, HttpServletRequest request, HttpServletResponse response, Object controller,
            Method controllerMethod);
}
