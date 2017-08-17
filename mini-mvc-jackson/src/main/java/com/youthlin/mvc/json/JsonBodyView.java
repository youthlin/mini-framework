package com.youthlin.mvc.json;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.mvc.listener.ControllerAndMethod;
import com.youthlin.mvc.support.View;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-17 23:55
 */
@Resource
public class JsonBodyView implements View {
    @Resource
    private JsonBodyHandler jsonBodyHandler;

    @Override
    public boolean render(HttpServletRequest request, HttpServletResponse response, Map<String, ?> model, Object result, ControllerAndMethod controllerAndMethod) throws Exception {
        if (AnnotationUtil.getAnnotation(controllerAndMethod.getMethod(), JsonBody.class) == null) {
            return false;
        }
        try {
            jsonBodyHandler.handler(request, response, result);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
