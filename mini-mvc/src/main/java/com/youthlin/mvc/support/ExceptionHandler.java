package com.youthlin.mvc.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 异常处理
 * 创建： youthlin.chen
 * 时间： 2017-08-14 16:23.
 */
public interface ExceptionHandler extends Order {
    /**
     * 当处理请求时发生异常将会调用此方法
     *
     * @param t                异常
     * @param controller       Controller 实例
     * @param controllerMethod Controller 中处理该请求的方法
     */
    void handler(Throwable t, HttpServletRequest request, HttpServletResponse response, Object controller,
            Method controllerMethod);
}
