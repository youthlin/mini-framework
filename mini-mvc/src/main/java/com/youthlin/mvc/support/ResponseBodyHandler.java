package com.youthlin.mvc.support;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-14 16:05.
 */
public interface ResponseBodyHandler extends Order {
    boolean accept(Method controllerMethod);

    void handler(HttpServletRequest request, HttpServletResponse response, Object result)
            throws ServletException, IOException;

}
