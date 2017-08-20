package com.youthlin.mvc.support;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-17 23:18
 */
public class DefaultResponseBodyHandler implements ResponseBodyHandler {
    @Override
    public boolean accept(Method controllerMethod) {
        return true;
    }

    @Override
    public void handler(HttpServletRequest request, HttpServletResponse response, Object result)
            throws ServletException, IOException {
        if (result != null) {
            response.getWriter().println(result.toString());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
