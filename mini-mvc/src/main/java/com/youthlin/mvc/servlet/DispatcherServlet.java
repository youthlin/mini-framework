package com.youthlin.mvc.servlet;

import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.exception.BeanInjectException;
import com.youthlin.mvc.annotation.ConvertWith;
import com.youthlin.mvc.annotation.HttpMethod;
import com.youthlin.mvc.annotation.Param;
import com.youthlin.mvc.converter.Converter;
import com.youthlin.mvc.converter.SimpleConverter;
import com.youthlin.mvc.listener.ContextLoaderListener;
import com.youthlin.mvc.listener.ControllerAndMethod;
import com.youthlin.mvc.listener.URLAndMethod;
import com.youthlin.mvc.support.DefaultView;
import com.youthlin.mvc.support.Interceptor;
import com.youthlin.mvc.support.Ordered;
import com.youthlin.mvc.support.View;
import com.youthlin.mvc.util.Java8ParameterNameDiscoverer;
import com.youthlin.mvc.util.JavaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 路由类，将各个请求分发至具体的 Controller 上的方法
 * 创建： youthlin.chen
 * 时间： 2017-08-13 15:43.
 */
@SuppressWarnings("WeakerAccess")
public class DispatcherServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);
    private ArrayList<Interceptor> interceptorList;
    private int interceptorIndex = -1;
    // 默认视图
    private static final View DEFAULT_VIEW = new DefaultView();

    public static Context getContext() {
        return ContextLoaderListener.getContext();
    }

    @SuppressWarnings("unchecked")
    public Map<URLAndMethod, ControllerAndMethod> getUrlMappingMap() {
        return (Map<URLAndMethod, ControllerAndMethod>) getServletContext().getAttribute(Constants.URL_MAPPING_MAP);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getMappedUrlSet() {
        return (Set<String>) getServletContext().getAttribute(Constants.MAPPED_URL_SET);
    }

    /**
     * 重写 service 方法.  当请求路径有映射的 Controller 时 将请求分发到 Controller 上
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqMethod = req.getMethod();
        String uri = req.getRequestURI();
        LOGGER.debug("{} {}", reqMethod, uri);
        ControllerAndMethod controllerAndMethod = findControllerAndMethod(uri, reqMethod);
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

    private ControllerAndMethod findControllerAndMethod(String requestURI, String reqMethod) {
        Map<URLAndMethod, ControllerAndMethod> urlMappingMap = getUrlMappingMap();
        reqMethod = reqMethod.toUpperCase();
        URLAndMethod urlAndMethod = new URLAndMethod(requestURI, HttpMethod.fromName(reqMethod));
        ControllerAndMethod controllerAndMethod = urlMappingMap.get(urlAndMethod);
        if (controllerAndMethod == null) {
            urlAndMethod = new URLAndMethod(requestURI);
            controllerAndMethod = urlMappingMap.get(urlAndMethod);
        }
        int lastIndexOfDot = requestURI.lastIndexOf(Constants.DOT);
        if (controllerAndMethod == null && lastIndexOfDot > 0) {// url:/get/some.html -> /get/some
            urlAndMethod = new URLAndMethod(requestURI.substring(0, lastIndexOfDot), HttpMethod.fromName(reqMethod));
            controllerAndMethod = urlMappingMap.get(urlAndMethod);
            if (controllerAndMethod == null) {
                urlAndMethod = new URLAndMethod(requestURI.substring(0, lastIndexOfDot));
                controllerAndMethod = urlMappingMap.get(urlAndMethod);
            }
        }
        return controllerAndMethod;
    }

    // ---------------------------------------------------------------------------------

    /**
     * 将请求打到 Controller 方法上
     */
    private void dispatch(HttpServletRequest req, HttpServletResponse resp, ControllerAndMethod controllerAndMethod)
            throws Throwable {
        HttpRequestWithModelMap request = new HttpRequestWithModelMap(req);
        Object controller = controllerAndMethod.getController();
        Method method = controllerAndMethod.getMethod();
        Throwable exception = null;
        try {
            Object[] parameter = injectParameter(request, resp, method);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("parameter: {}", Arrays.deepToString(parameter));
            }
            if (!preHandle(request, resp, controller)) {
                return;
            }
            Object ret = method.invoke(controller, parameter);
            postHandle(request, resp, controller, ret);
            Map<String, Object> model = request.getMap();
            LOGGER.debug("invoke ret: {}", ret);
            processInvokeResult(request, resp, model, ret, controllerAndMethod);
        } catch (Throwable e) {
            exception = e;// throw
        } finally {
            exception = afterCompletion(request, resp, controller, exception);
            if (exception != null) {
                throw exception;
            }
        }
    }

    private Object[] injectParameter(HttpServletRequest req, HttpServletResponse resp, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();//每个参数的类型
        //每个参数的 Param 注解 如果第零个参数没有 Param 注解 那么 params[0] 为 null
        Param[] params = getParams(method, parameterTypes);
        Object[] parameter = new Object[parameterTypes.length];//调用方法的实参
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType.isAssignableFrom(HttpServletRequest.class)) {
                parameter[i] = req;
            } else if (parameterType.isAssignableFrom(HttpServletResponse.class)) {
                parameter[i] = resp;
            } else if (parameterType.isAssignableFrom(Map.class)) {
                HashMap<String, Object> map = new ModelWithRequest(req);
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
                Param param = params[i];
                parameter[i] = getParameterValue(req, method, i, parameterType, param, getParameterName(method, param, i));
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

    /**
     * 优先使用 {@link Param} 注解获取参数名，如果没有注解，尝试使用 Java8 反射获取参数名，否则使用 arg1, arg2... 作为参数名
     */
    private String getParameterName(Method method, Param param, int index) {
        if (param != null) {//有注解
            String name = param.name();
            if (name.isEmpty()) {
                name = param.value();
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name of Param should be specified. " + param);
            }
            return name;
        }
        if (JavaVersion.supportJava8()) {
            Java8ParameterNameDiscoverer java8ParameterNameDiscoverer = getContext().getBean(Java8ParameterNameDiscoverer.class);
            if (java8ParameterNameDiscoverer == null) {
                java8ParameterNameDiscoverer = new Java8ParameterNameDiscoverer();
                getContext().registerBean(java8ParameterNameDiscoverer);
            }
            String[] parameterNames = java8ParameterNameDiscoverer.getParameterNames(method);
            return parameterNames[index];
        }
        return "arg" + (index + 1);
    }

    /**
     * 获取 Controller 参数的值
     * 若 request 里直接有这个参数, 直接转换
     * 否则:
     * - 认为这个参数是个 Pojo, 对其每个字段注入值;
     * - 注入字段时, 若 request 有字段名字, 直接转换,
     * - - 否则再次注入 pojo, 拼上 filedName.innerFiledName 查找请求参数
     * <pre>
     *     // id; name
     *     public String hello(int id, String name){}
     * ---------------------------------------------------------------
     *     public class User{
     *         int id;
     *         String name;
     *     }
     *     //id; user.id; user.name
     *     public String hello(int id, User user){}
     * ---------------------------------------------------------------
     *
     *     ;@Resource
     *     public class MyConverter implements Converter{
     *         public User convert(String str, User.class){
     *             return JsonUtil.from(str, User.class);
     *         }
     *     }
     *     //id="1"; user="{id=1,name='xxx'}"
     *     public String hello(int id, @ConvertWith(MyConverter.class) User user){}
     *   ---------------------------------------------------------------
     *
     *     public class User{
     *         int id;
     *         ;@ConverterWith(CatConverter.class) Cat cat;
     *     }
     *     //id=1; user.id=1, user.cat="xxx"
     *     public String hello(int id, User user){}
     * </pre>
     *
     * @param request 请求
     * @param method  Controller 的方法
     * @param index   第几个参数
     * @param type    参数的类型
     * @param param   参数的注解 可能为 null
     * @param name    注解参数给这个参数的名称; param==null 时, 是参数本身的名称(Java8)或 arg1, arg2...
     * @return 这个参数的值, 可能为 null
     * @throws IllegalArgumentException 如果有 Param 注解, 且是必填项, 但值为 null 则抛出异常
     */
    private Object getParameterValue(HttpServletRequest request, Method method,
            int index, Class<?> type, Param param, String name) {
        Object result = null;
        ConvertWith convertWith = getConvertWith(method, index);
        Converter converter = null;
        if (convertWith != null) {
            converter = getConverter(convertWith, type);
        }

        String stringValue = request.getParameter(name);
        if (stringValue != null) {//request 有这个参数 直接转换
            return convert(stringValue, converter, type);
        }

        //request 没有这个参数 一层层构造
        try {
            Constructor<?> constructor = type.getConstructor();
            result = constructor.newInstance();
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                injectFiled(type, result, field, name, request);
            }
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Class {} has no default Constructor", type, e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOGGER.warn("Can not create instance of {}", type, e);
        }

        if (result == null) {
            if (param.required()) {
                throw new IllegalArgumentException(name + " is required");
            }
            String defaultValue = param.defaultValue();
            return convert(defaultValue, converter, type);
        }
        return result;
    }

    private void injectFiled(Class<?> classType, Object targetObject, Field field, String prefix,
            HttpServletRequest request) {
        int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            return;//final 不能设置值
        }
        String fieldName = field.getName();
        String value = request.getParameter(prefix + "." + fieldName);
        Class<?> fieldType = field.getType();
        if (value == null) {
            //todo
            return;//request 没有对应的值
        }
        Converter converter = getSimpleConverter();//默认转换器
        ConvertWith convertWith = field.getAnnotation(ConvertWith.class);
        if (convertWith != null) {//自定义转换器
            converter = getConverter(convertWith, field);
        }
        Object converted = convert(value, converter, fieldType);
        //有 set 方法 优先使用 set 方法设值
        Method setMethod = null;
        try {
            String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            setMethod = classType.getMethod(setMethodName, fieldType);
            setMethod.invoke(targetObject, converted);
        } catch (NoSuchMethodException ignore) {
            try {//没有 set 方法 直接设置
                field.setAccessible(true);
                field.set(targetObject, converted);
            } catch (IllegalAccessException e) {
                LOGGER.warn("Can not set filed {} of {} from {}(From HttpRequest) {}(after convert)",
                        fieldName, classType, value, converted, e);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Invoke setter method error: {} String value from request: {}, after convert:{}",
                    setMethod, value, converted, e);
        }
    }

    private Object convert(String str, Converter converter, Class<?> type) {
        if (converter != null) {
            return converter.convert(str, type);
        }
        return getSimpleConverter().convert(str, type);
    }


    private ConvertWith getConvertWith(Method method, int index) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (Annotation annotation : parameterAnnotations[index]) {
            if (annotation instanceof ConvertWith) {
                return (ConvertWith) annotation;
            }
        }
        return null;
    }

    private Converter getConverter(ConvertWith convertWith, AnnotatedElement annotatedElement) {
        Class<? extends Converter> converterClass = convertWith.value();
        Converter converter = getContext().getBean(converterClass);
        if (converter == null) {
            throw new UnsupportedOperationException("Converter not found: " + converterClass + " (" + annotatedElement + ')');
        }
        return converter;
    }

    private static SimpleConverter getSimpleConverter() {
        SimpleConverter converter = getContext().getBean(SimpleConverter.class);
        if (converter == null) {
            converter = new SimpleConverter();
            getContext().registerBean(converter);
        }
        return converter;
    }

    private <T> T injectJavaBean(HttpServletRequest request, Class<T> classType) {
        T targetObject;
        //要求有无参构造器
        try {
            Constructor<T> constructor = classType.getConstructor();
            targetObject = constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + classType + " has no default Constructor", e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new BeanInjectException("Can not create instance of " + classType, e);
        }
        Field[] fields = classType.getDeclaredFields();
        for (Field field : fields) {//对每个字段设置值

        }//for field
        return targetObject;
    }

    // ---------------------------------------------------------------------------------

    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object controller)
            throws Exception {
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

    protected void postHandle(HttpServletRequest request, HttpServletResponse response, Object controller,
            Object result) throws Exception {
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
    protected void processInvokeResult(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> model,
            Object result, ControllerAndMethod controllerAndMethod) throws Throwable {
        if (result instanceof String &&
                (((String) result).startsWith(Constants.FORWARD) || ((String) result).startsWith(Constants.REDIRECT))) {
            processRedirectOrForward(req, resp, model, (String) result, controllerAndMethod);
            return;
        }
        List<View> sortedViewList = new ArrayList<>(getContext().getBeans(View.class));
        Collections.sort(sortedViewList, Ordered.DEFAULT_ORDERED_COMPARATOR);
        boolean rendered = false;
        for (View view : sortedViewList) {
            rendered = view.render(req, resp, model, result, controllerAndMethod);
            if (rendered) {
                break;
            }
        }
        if (!rendered) {
            DEFAULT_VIEW.render(req, resp, model, result, controllerAndMethod);
        }
    }

    protected void processRedirectOrForward(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> model,
            String result, ControllerAndMethod controllerAndMethod) throws Throwable {
        if (result.startsWith(Constants.REDIRECT)) {
            resp.sendRedirect(req.getContextPath() + result.substring(Constants.REDIRECT.length()));
        } else if (result.startsWith(Constants.FORWARD)) {
            String requestURI = result.substring(Constants.FORWARD.length());
            String reqMethod = req.getMethod();
            ControllerAndMethod forwardHandler = findControllerAndMethod(requestURI, reqMethod);
            if (forwardHandler != null) {
                dispatch(req, resp, forwardHandler);
            } else {
                req.getRequestDispatcher(requestURI).forward(req, resp);
            }
        }
    }

    protected Throwable afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Throwable e) {
        List<Interceptor> sortedInterceptors = getSortedInterceptors();
        String uri = req.getRequestURI();
        for (int i = interceptorIndex; i >= 0; i--) {
            Interceptor interceptor = sortedInterceptors.get(i);
            if (interceptor.accept(uri)) {
                try {
                    e = interceptor.afterCompletion(req, resp, handler, e);
                } catch (Throwable t) {
                    LOGGER.error("HandlerInterceptor.afterCompletion threw exception", t);
                }
            }
        }
        return e;
    }

    public ArrayList<Interceptor> getSortedInterceptors() {
        Set<Interceptor> interceptorSet = getContext().getBeans(Interceptor.class);
        if (interceptorList == null || interceptorSet.size() != interceptorList.size()) {//需要初始化或更新List
            interceptorList = new ArrayList<>();
            interceptorList.addAll(interceptorSet);
            Collections.sort(interceptorList, Ordered.DEFAULT_ORDERED_COMPARATOR);
        }
        return interceptorList;
    }

    // ---------------------------------------------------------------------------------

    /**
     * 没有匹配到 Controller
     */
    protected void processNoMatch(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        @SuppressWarnings("unchecked")
        Set<String> mappedUrls = (Set<String>) getServletContext().getAttribute(Constants.MAPPED_URL_SET);
        String requestURI = req.getRequestURI();
        boolean containsURI = mappedUrls.contains(requestURI);
        if (!containsURI) {
            int lastIndexOfDot = requestURI.lastIndexOf(Constants.DOT);
            if (lastIndexOfDot > 0) {
                requestURI = requestURI.substring(0, lastIndexOfDot);
                containsURI = mappedUrls.contains(requestURI);
            }
        }
        if (!containsURI) {
            sendError404(req, resp);
            return;
        }
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
        Map<URLAndMethod, ControllerAndMethod> urlMappingMap = getUrlMappingMap();
        String requestURI = req.getRequestURI();
        URLAndMethod urlAndMethod = new URLAndMethod(requestURI, HttpMethod.GET);
        ControllerAndMethod controllerAndMethod = urlMappingMap.get(urlAndMethod);
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
        Map<URLAndMethod, ControllerAndMethod> urlMappingMap = getUrlMappingMap();
        URLAndMethod urlAndMethod = new URLAndMethod(requestUri, method);
        return urlMappingMap.get(urlAndMethod) != null;
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

    private void sendError404(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public void destroy() {
        ContextLoaderListener.preDestroy();
        super.destroy();
    }
}
