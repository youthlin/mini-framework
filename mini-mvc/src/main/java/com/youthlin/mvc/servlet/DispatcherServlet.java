package com.youthlin.mvc.servlet;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.context.Context;
import com.youthlin.mvc.annotation.HttpMethod;
import com.youthlin.mvc.annotation.Param;
import com.youthlin.mvc.annotation.ResponseBody;
import com.youthlin.mvc.listener.ContextLoaderListener;
import com.youthlin.mvc.mapping.ControllerAndMethod;
import com.youthlin.mvc.mapping.URLAndMethods;
import com.youthlin.mvc.support.Interceptor;
import com.youthlin.mvc.support.Ordered;
import com.youthlin.mvc.support.ResponseBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;


/**
 * 路由类，将各个请求分发至具体的 Controller 上的方法
 * 创建： youthlin.chen
 * 时间： 2017-08-13 15:43.
 */
@SuppressWarnings("WeakerAccess")
public class DispatcherServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);
    public static final String REDIRECT = "redirect:";
    public static final String FORWARD = "forward:";
    private ArrayList<Interceptor> interceptorList;
    private int interceptorIndex = -1;
    /**
     * 默认 ResponseBody 处理器, 直接将返回值 toString 输出
     */
    private static final ResponseBodyHandler DEFAULT_RESPONSE_BODY_HANDLER = new ResponseBodyHandler() {
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
    };

    public Context getContext() {
        return (Context) super.getServletContext().getAttribute(ContextLoaderListener.CONTAINER);
    }

    @SuppressWarnings("unchecked")
    public Map<URLAndMethods, ControllerAndMethod> getUrlMappingMap() {
        return (Map<URLAndMethods, ControllerAndMethod>)
                super.getServletContext().getAttribute(ContextLoaderListener.URL_MAPPING_MAP);
    }

    /**
     * 重写 service 方法.  当请求路径有映射的 Controller 时 将请求分发到 Controller 上
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqMethod = req.getMethod();
        String uri = req.getRequestURI();
        LOGGER.debug("uri = {}, method = {}", uri, reqMethod);
        @SuppressWarnings("unchecked")
        Map<URLAndMethods, ControllerAndMethod> urlMappingMap = getUrlMappingMap();
        URLAndMethods urlAndMethods = new URLAndMethods(uri, URLAndMethods.method(reqMethod));
        ControllerAndMethod controllerAndMethod = urlMappingMap.get(urlAndMethods);
        if (controllerAndMethod == null) {
            urlAndMethods = new URLAndMethods(uri, URLAndMethods.EMPTY_HTTP_METHODS);
            controllerAndMethod = urlMappingMap.get(urlAndMethods);
        }
        int lastIndexOfDot = uri.lastIndexOf(".");
        if (controllerAndMethod == null && lastIndexOfDot > 0) {// url:/get/some.html -> /get/some
            urlAndMethods = new URLAndMethods(uri.substring(0, lastIndexOfDot), URLAndMethods.method(reqMethod));
            controllerAndMethod = urlMappingMap.get(urlAndMethods);
            if (controllerAndMethod == null) {
                urlAndMethods = new URLAndMethods(uri.substring(0, lastIndexOfDot), URLAndMethods.EMPTY_HTTP_METHODS);
                controllerAndMethod = urlMappingMap.get(urlAndMethods);
            }
        }
        try {
            if (controllerAndMethod != null) {
                dispatch(req, resp, controllerAndMethod);
            } else {
                processNoMatch(req, resp);
            }
        } catch (Throwable e) {
            if (e instanceof ServletException) {
                throw (ServletException) e;
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new ServletException(e);
        }
    }

    /**
     * 将请求打到 Controller 方法上
     */
    private void dispatch(HttpServletRequest req, HttpServletResponse resp, ControllerAndMethod controllerAndMethod) throws Throwable {
        Object controller = controllerAndMethod.getController();
        Method method = controllerAndMethod.getMethod();
        Throwable exception = null;
        try {
            Object[] parameter = injectParameter(req, resp, method);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("parameter: {}", Arrays.deepToString(parameter));
            }
            if (!preHandle(req, resp, controller)) {
                return;
            }
            Object ret = method.invoke(controller, parameter);
            postHandle(req, resp, controller, ret);

            LOGGER.debug("invoke ret: {}", ret);
            processInvokeResult(ret, req, resp, controllerAndMethod);
        } catch (Throwable e) {
            exception = e;
        } finally {
            afterCompletion(req, resp, controller, exception);
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
                if (param == null) {
                    continue;
                }
                String value = getParameter(req, param);
                if (parameterType.isAssignableFrom(String.class)) {
                    parameter[i] = getParameter(req, param);
                } else if (parameterType.isAssignableFrom(double.class) || parameterType
                        .isAssignableFrom(Double.class)) {
                    parameter[i] = Double.parseDouble(value);
                } else if (parameterType.isAssignableFrom(float.class) || parameterType.isAssignableFrom(Float.class)) {
                    parameter[i] = Float.parseFloat(value);
                } else if (parameterType.isAssignableFrom(long.class) || parameterType.isAssignableFrom(Long.class)) {
                    parameter[i] = Long.parseLong(value);
                } else if (parameterType.isAssignableFrom(int.class) || parameterType.isAssignableFrom(Integer.class)) {
                    parameter[i] = Integer.parseInt(value);
                } else if (parameterType.isAssignableFrom(short.class) || parameterType.isAssignableFrom(Short.class)) {
                    parameter[i] = Short.parseShort(value);
                } else if (parameterType.isAssignableFrom(byte.class) || parameterType.isAssignableFrom(Byte.class)) {
                    parameter[i] = Byte.parseByte(value);
                } else if (parameterType.isAssignableFrom(boolean.class)
                        || parameterType.isAssignableFrom(Boolean.class)) {
                    parameter[i] = Boolean.parseBoolean(value);
                } else if (parameterType.isAssignableFrom(char.class)
                        || parameterType.isAssignableFrom(Character.class)) {
                    if (value.length() == 1) {
                        parameter[i] = value.charAt(0);
                    } else {
                        throw new IllegalArgumentException('\"' + value + "\" can not cast to char.");
                    }
                } else {
                    parameter[i] = injectJavaBean(req, parameterType);
                }
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
            throw new IllegalArgumentException("parameter \"" + name + "\" is required.");
        }
        String defaultValue = param.defaultValue();
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private <T> T injectJavaBean(HttpServletRequest request, Class<T> classType) {
        return null;//todo 支持在方法列表上直接写 POJO
    }

    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object controller) throws Exception {
        ArrayList<Interceptor> interceptors = getSortedInterceptors();
        String uri = request.getRequestURI();
        int size = interceptors.size();
        interceptorIndex = -1;
        for (int i = 0; i < size; i++) {
            Interceptor interceptor = interceptors.get(i);
            if (interceptor.accept(uri)) {
                if (!interceptor.preHandle(request, response, controller)) {
                    return false;
                }
            }
            interceptorIndex = i;

        }
        return true;
    }

    protected void postHandle(HttpServletRequest request, HttpServletResponse response, Object controller, Object result) throws Exception {
        ArrayList<Interceptor> interceptors = getSortedInterceptors();
        String uri = request.getRequestURI();
        for (Interceptor interceptor : interceptors) {
            if (interceptor.accept(uri)) {
                interceptor.postHandle(request, response, controller, result);
            }
        }
    }

    /**
     * 处理 Controller 方法返回值
     */
    protected void processInvokeResult(Object result, HttpServletRequest req, HttpServletResponse resp,
                                       ControllerAndMethod controllerAndMethod) throws IOException, ServletException {
        Method method = controllerAndMethod.getMethod();
        ResponseBody responseBody = AnnotationUtil.getAnnotation(method, ResponseBody.class);
        if (responseBody != null) {
            Context context = getContext();
            TreeSet<ResponseBodyHandler> responseBodyHandlers = new TreeSet<>(Ordered.DEFAULT_ORDERED_COMPARATOR);
            responseBodyHandlers.addAll(context.getBeans(ResponseBodyHandler.class));
            boolean processed = false;
            for (ResponseBodyHandler responseBodyHandler : responseBodyHandlers) {
                if (responseBodyHandler.accept(method)) {
                    processed = true;
                    responseBodyHandler.handler(req, resp, result);
                }
            }
            if (!processed) {
                DEFAULT_RESPONSE_BODY_HANDLER.handler(req, resp, result);
            }
        } else {
            //返回字符串：页面
            if (result instanceof String) {
                if (((String) result).startsWith(REDIRECT)) {
                    resp.sendRedirect(((String) result).substring(REDIRECT.length()));
                    return;
                } else if (((String) result).startsWith(FORWARD)) {
                    req.getRequestDispatcher(((String) result).substring(FORWARD.length())).forward(req, resp);
                    return;
                }
                String prefix = (String) super.getServletContext().getAttribute(ContextLoaderListener.VIEW_PREFIX);
                String suffix = (String) super.getServletContext().getAttribute(ContextLoaderListener.VIEW_SUFFIX);
                req.getRequestDispatcher(prefix + result + suffix).forward(req, resp);
            } else {
                throw new RuntimeException(
                        "You can only return String value when there is no @ResponseBody on method.");
            }
        }
    }


    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object controller, Throwable e) {
        List<Interceptor> sortedInterceptors = getSortedInterceptors();
        String uri = request.getRequestURI();
        for (int i = interceptorIndex; i >= 0; i--) {
            Interceptor interceptor = sortedInterceptors.get(i);
            if (interceptor.accept(uri)) {
                try {
                    interceptor.afterCompletion(request, response, controller, e);
                } catch (Throwable t) {
                    LOGGER.error("HandlerInterceptor.afterCompletion threw exception", t);
                }
            }
        }
    }

    /**
     * 没有匹配到 Controller
     */
    protected void processNoMatch(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        String method = req.getMethod();
        switch (method) {
            case "HEAD":
                processHead(req, resp);
                break;
            case "OPTIONS":
                processOptions(req, resp);
                break;
            case "TRACE":
                super.doTrace(req, resp);
                break;
            case "GET":
            case "POST":
            case "PUT":
            case "PATCH":
            case "DELETE":
            default:
                sendError405(req, resp);
        }
    }

    private void processHead(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        @SuppressWarnings("unchecked")
        Map<URLAndMethods, ControllerAndMethod> urlMappingMap = getUrlMappingMap();
        String requestURI = req.getRequestURI();
        URLAndMethods urlAndMethods = new URLAndMethods(requestURI, URLAndMethods.HTTP_METHODS_GET);
        ControllerAndMethod controllerAndMethod = urlMappingMap.get(urlAndMethods);
        if (controllerAndMethod == null) {
            sendError405(req, resp);
        } else {
            //all data write to response is only to count length but not send to client
            NoBodyResponse response = new NoBodyResponse(resp);
            dispatch(req, response, controllerAndMethod);//doGet
            response.setContentLength();
        }
    }

    private void processOptions(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        @SuppressWarnings("unchecked")
        String requestURI = req.getRequestURI();
        StringBuilder allow = new StringBuilder();
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (supportHttpMethod(requestURI, httpMethod)) {
                if (allow.length() > 0) {
                    allow.append(", ");
                }
                allow.append(httpMethod.name());
            }
        }
        resp.setHeader("Allow", allow.toString());
    }

    private boolean supportHttpMethod(String requestUri, HttpMethod method) {
        switch (method) {
            case HEAD:
                return supportHttpMethod(requestUri, HttpMethod.GET);
            case TRACE:
            case OPTIONS:
                return true;
        }
        Map<URLAndMethods, ControllerAndMethod> urlMappingMap = getUrlMappingMap();
        URLAndMethods urlAndMethods = new URLAndMethods(requestUri, new HttpMethod[]{method});
        return urlMappingMap.get(urlAndMethods) != null;
    }

    private void sendError405(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String protocol = request.getProtocol();
        String method = request.getMethod();
        String msg = "Http method " + method + " is not supported by this URL";
        if (protocol.endsWith("1.1")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
    }

    public ArrayList<Interceptor> getSortedInterceptors() {
        Set<Interceptor> interceptorSet = getContext().getBeans(Interceptor.class);
        if (interceptorList == null || interceptorSet.size() != interceptorList.size()) {
            interceptorList = new ArrayList<>();
            interceptorList.addAll(interceptorSet);
            Collections.sort(interceptorList, Ordered.DEFAULT_ORDERED_COMPARATOR);
        }
        return interceptorList;
    }

}
