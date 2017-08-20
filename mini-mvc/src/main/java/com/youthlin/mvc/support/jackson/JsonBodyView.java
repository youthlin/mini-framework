package com.youthlin.mvc.support.jackson;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.mvc.listener.ControllerAndMethod;
import com.youthlin.mvc.support.ResponseBodyHandler;
import com.youthlin.mvc.support.View;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-17 23:55
 */
@Resource//没有引入jackson的话jsonBodyHandler就会new失败，这个类就不会加入容器
public class JsonBodyView implements View {
    private static final ResponseBodyHandler jsonBodyHandler = new JsonBodyHandler();

    @Override
    public boolean render(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model, Object result, ControllerAndMethod controllerAndMethod) throws Exception {
        if (AnnotationUtil.getAnnotation(controllerAndMethod.getMethod(), JsonBody.class) == null) {
            return false;
        }
        jsonBodyHandler.handler(request, response, result);//if has exception, just throws out
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
