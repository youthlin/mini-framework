package com.youthlin.mvc.servlet;

import com.youthlin.mvc.annotation.Param;
import com.youthlin.mvc.listener.ContextLoaderListener;
import com.youthlin.mvc.mapping.ControllerAndMethod;
import com.youthlin.mvc.mapping.URLAndMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 15:43.
 */
@SuppressWarnings("WeakerAccess")
public class DispatcherServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);

    /**
     * 重写 service 方法.  当请求路径有映射的 Controller 时 将请求分发到 Controller 上
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqMethod = req.getMethod();
        String uri = req.getRequestURI();
        LOGGER.debug("uri = {}, method = {}", uri, reqMethod);
        @SuppressWarnings("unchecked")
        Map<URLAndMethods, ControllerAndMethod> urlMappingMap = (Map<URLAndMethods, ControllerAndMethod>)
                super.getServletContext().getAttribute(ContextLoaderListener.URL_MAPPING_MAP);
        URLAndMethods urlAndMethods = new URLAndMethods(uri, URLAndMethods.method(reqMethod));
        ControllerAndMethod controllerAndMethod = urlMappingMap.get(urlAndMethods);
        if (controllerAndMethod == null) {
            urlAndMethods = new URLAndMethods(uri, URLAndMethods.EMPTY_METHODS);
            controllerAndMethod = urlMappingMap.get(urlAndMethods);
        }
        int lastIndexOfDot = uri.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            urlAndMethods = new URLAndMethods(uri.substring(0, lastIndexOfDot), URLAndMethods.method(reqMethod));
            controllerAndMethod = urlMappingMap.get(urlAndMethods);
            if (controllerAndMethod == null) {
                urlAndMethods = new URLAndMethods(uri.substring(0, lastIndexOfDot), URLAndMethods.EMPTY_METHODS);
                controllerAndMethod = urlMappingMap.get(urlAndMethods);
            }
        }
        if (controllerAndMethod != null) {
            dispatch(req, resp, controllerAndMethod);
            return;
        }
        processNoMatch(req, resp);
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp, ControllerAndMethod controllerAndMethod)
            throws ServletException, IOException {
        Object controller = controllerAndMethod.getController();
        Method method = controllerAndMethod.getMethod();
        try {
            Object[] parameter = injectParameter(req, resp, method);
            Object ret = method.invoke(controller, parameter);
            LOGGER.debug("invoke ret: {}", ret);
            peocessInvokeResult(ret, req, resp, controllerAndMethod);
        } catch (Throwable e) {
            LOGGER.error("{}", controllerAndMethod, e);
            throw new RuntimeException(e);
        }
    }

    private Object[] injectParameter(HttpServletRequest req, HttpServletResponse resp, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();//每个参数的类型
        //每个参数的 Param 注解 如果第零个参数没有 Param 注解 那么 params[0] 为 null
        Param[] params = getParams(method, parameterTypes);
        Object[] parameter = new Object[parameterTypes.length];//调用方法的实参

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Param param = params[i];
            if (parameterType.isAssignableFrom(HttpServletRequest.class)) {
                parameter[i] = req;
            } else if (parameterType.isAssignableFrom(HttpServletResponse.class)) {
                parameter[i] = resp;
            } else if (parameterType.isAssignableFrom(String.class)) {
                if (param == null) {
                    continue;
                }
                parameter[i] = getParameter(req, param);
            } else if (parameterType.isPrimitive()) {
                if (param == null) {
                    continue;
                }
                String value = getParameter(req, param);
                if (parameterType.isAssignableFrom(double.class)) {
                    parameter[i] = Double.parseDouble(value);
                } else if (parameterType.isAssignableFrom(float.class)) {
                    parameter[i] = Float.parseFloat(value);
                } else if (parameterType.isAssignableFrom(long.class)) {
                    parameter[i] = Long.parseLong(value);
                } else if (parameterType.isAssignableFrom(int.class)) {
                    parameter[i] = Integer.parseInt(value);
                } else if (parameterType.isAssignableFrom(short.class)) {
                    parameter[i] = Short.parseShort(value);
                } else if (parameterType.isAssignableFrom(byte.class)) {
                    parameter[i] = Byte.parseByte(value);
                } else if (parameterType.isAssignableFrom(boolean.class)) {
                    parameter[i] = Boolean.parseBoolean(value);
                } else if (parameterType.isAssignableFrom(char.class)) {
                    if (value.length() == 1) {
                        parameter[i] = value.charAt(0);
                    } else {
                        throw new IllegalArgumentException(value + " can not cast to char.");
                    }
                }
            } else if (parameterType.isAssignableFrom(Map.class)) {
                HashMap<String, Object> map = new HashMap<>();
                parameter[i] = map;
                Enumeration<String> parameterNames = req.getParameterNames();
                while (parameterNames.hasMoreElements()) {
                    String parameterName = parameterNames.nextElement();
                    String[] parameterValues = req.getParameterValues(parameterName);
                    if (parameterValues.length == 1) {
                        map.put(parameterName, parameterValues[0]);
                    } else {
                        map.put(parameterName, parameterValues);
                    }
                }
            } else {
                parameter[i] = injectJavaBean(req, parameterType);
            }
        }
        return parameter;
    }

    private Param[] getParams(Method method, Class[] parameterTypes) {
        Param[] params = new Param[parameterTypes.length];
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("method {} parameterAnnotations: {}", method, Arrays.deepToString(parameterAnnotations));
        }
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof Param) {
                    params[i] = (Param) annotation;
                    break;
                }
            }
        }
        return params;
    }

    private String getParameter(HttpServletRequest request, Param param) {
        String name = param.name();
        if (name.isEmpty()) {
            name = param.value();
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name of Param should be specified." + param);
        }
        String value = request.getParameter(name);
        if (param.required() && value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        String defaultValue = param.defaultValue();
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private <T> T injectJavaBean(HttpServletRequest request, Class<T> classType) {
        return null;
    }

    protected void processNoMatch(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        out.println(req.getMethod() + " " + req.getRequestURI());
        out.println("No matched Controller.");
    }

    protected void peocessInvokeResult(Object result, HttpServletRequest req, HttpServletResponse resp,
            ControllerAndMethod controllerAndMethod) {

    }
}
