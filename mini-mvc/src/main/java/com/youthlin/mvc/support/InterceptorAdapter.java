package com.youthlin.mvc.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-16 00:05
 */
public class InterceptorAdapter implements Interceptor {
    @Override
    public boolean accept(String uri) {
        return true;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object controller) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object controller, Object result) throws Exception {
    }

    @Override
    public Throwable afterCompletion(HttpServletRequest request, HttpServletResponse response, Object controller, Throwable e) throws Throwable {
        return e;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
