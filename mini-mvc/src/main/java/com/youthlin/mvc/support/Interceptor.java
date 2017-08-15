package com.youthlin.mvc.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器
 * 创建：youthlin.chen
 * 时间：2017-08-15 23:15
 */
public interface Interceptor extends Ordered {
    /**
     * 是否拦截当前URL
     */
    boolean accept(String uri);

    /**
     * 是否继续其他
     */
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object controller) throws Exception;

    void postHandle(HttpServletRequest request, HttpServletResponse response, Object controller, Object result) throws Exception;

    void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object controller, Throwable e) throws Throwable;

}
