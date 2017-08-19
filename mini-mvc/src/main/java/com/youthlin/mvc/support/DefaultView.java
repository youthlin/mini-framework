package com.youthlin.mvc.support;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.context.Context;
import com.youthlin.mvc.annotation.ResponseBody;
import com.youthlin.mvc.listener.ControllerAndMethod;
import com.youthlin.mvc.servlet.Constants;
import com.youthlin.mvc.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-17 23:16
 */
public class DefaultView implements View {
    private static final ResponseBodyHandler DEFAULT_RESPONSE_BODY_HANDLER = new DefaultResponseBodyHandler();

    @Override
    public boolean render(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> model, Object result,
            ControllerAndMethod controllerAndMethod) throws Exception {
        Method method = controllerAndMethod.getMethod();
        ResponseBody responseBody = AnnotationUtil.getAnnotation(method, ResponseBody.class);
        if (responseBody != null) {
            Context context = DispatcherServlet.getContext(req);
            List<ResponseBodyHandler> responseBodyHandlerList = new ArrayList<>(
                    context.getBeans(ResponseBodyHandler.class));
            Collections.sort(responseBodyHandlerList, Ordered.DEFAULT_ORDERED_COMPARATOR);
            boolean processed = false;
            for (ResponseBodyHandler responseBodyHandler : responseBodyHandlerList) {
                if (responseBodyHandler.accept(method)) {
                    processed = true;
                    responseBodyHandler.handler(req, resp, result);
                    break;
                }
            }
            if (!processed) {
                DEFAULT_RESPONSE_BODY_HANDLER.handler(req, resp, result);
            }
        } else {
            //返回字符串：页面 redirect 和 forward 已经在 DispatcherServlet 中处理了
            if (result instanceof String) {
                String prefix = (String) req.getServletContext().getAttribute(Constants.VIEW_PREFIX);
                String suffix = (String) req.getServletContext().getAttribute(Constants.VIEW_SUFFIX);
                req.getRequestDispatcher(prefix + result + suffix).forward(req, resp);
            } else {
                throw new RuntimeException(
                        "You can only return String value when there is no @ResponseBody on method.");
            }
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
