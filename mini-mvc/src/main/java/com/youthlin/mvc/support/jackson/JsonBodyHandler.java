package com.youthlin.mvc.support.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.mvc.support.ResponseBodyHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-18 10:48.
 */
public class JsonBodyHandler implements ResponseBodyHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public boolean accept(Method controllerMethod) {
        return AnnotationUtil.getAnnotation(controllerMethod, JsonBody.class) != null;
    }

    @Override
    public void handler(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(objectMapper.writeValueAsBytes(result));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
