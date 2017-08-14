package com.youthlin.mvc.listener;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.Controller;
import com.youthlin.ioc.context.ClasspathContext;
import com.youthlin.ioc.context.Context;
import com.youthlin.mvc.annotation.URL;
import com.youthlin.mvc.mapping.ControllerAndMethod;
import com.youthlin.mvc.mapping.URLAndMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 13:20.
 */
@SuppressWarnings("WeakerAccess")
public class ContextLoaderListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextLoaderListener.class);
    private static final String FORWARD = "/";
    public static final String CONTAINER = "_MINI_IOC_CONTAINER";
    public static final String URL_MAPPING_MAP = "_URL_MAPPING_MAP";
    public static final String VIEW_PREFIX = "_VIEW_PREFIX";
    public static final String VIEW_PREFIX_PARAM_NAME = "view-prefix";
    public static final String VIEW_SUFFIX = "_VIEW_SUFFIX";
    public static final String VIEW_SUFFIX_PARAM_NAME = "view-suffix";
    private Context container;
    private Map<URLAndMethods, ControllerAndMethod> urlMapping = new ConcurrentHashMap<>();

    public ContextLoaderListener() {
        LOGGER.debug("构造 ContextLoaderListener");
    }

    /**
     * 容器启动时自动执行
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.debug("contextInitialized sce = {}", sce);
        init(sce);
        mapping(sce);
    }

    //初始化容器
    private void init(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        LOGGER.debug("servlet context = {}, source = {}", servletContext, sce.getSource());
        servletContext.setAttribute(VIEW_PREFIX, "");
        servletContext.setAttribute(VIEW_SUFFIX, "");
        Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String parameterName = initParameterNames.nextElement();
            String initParameterValue = servletContext.getInitParameter(parameterName);
            if (parameterName.equals(VIEW_PREFIX_PARAM_NAME)) {
                servletContext.setAttribute(VIEW_PREFIX, initParameterValue);
            } else if (parameterName.equals(VIEW_SUFFIX_PARAM_NAME)) {
                servletContext.setAttribute(VIEW_SUFFIX, initParameterValue);
            }
            LOGGER.debug("name = {}, value = {}", parameterName, initParameterValue);
        }
        String scan = servletContext.getInitParameter("scan");
        String[] scanPackages = { "" };
        if (scan != null) {
            scanPackages = scan.split("\\s|,|;");
        }
        container = new ClasspathContext(scanPackages);
        LOGGER.debug("register {} beans.", container.getBeanCount());
        servletContext.setAttribute(CONTAINER, container);
    }

    //映射 URL 到 Controller 方法
    private void mapping(ServletContextEvent sce) {
        for (Object bean : container.getBeans()) {
            Class<?> beanClass = bean.getClass();
            Controller controller = AnnotationUtil.getAnnotation(beanClass, Controller.class);
            if (controller != null) {
                URL controllerUrl = AnnotationUtil.getAnnotation(beanClass, URL.class);
                String urlPrefix = null;
                if (controllerUrl != null) {
                    urlPrefix = (String) AnnotationUtil.getValue(beanClass, controllerUrl);
                }
                Method[] methods = beanClass.getMethods();
                if (methods != null) {
                    for (Method method : methods) {
                        if (method.getModifiers() != Modifier.PUBLIC) {
                            continue;
                        }
                        URL urlAnnotation = AnnotationUtil.getAnnotation(method, URL.class);
                        if (urlAnnotation != null) {
                            String url = (String) AnnotationUtil.getValue(method, urlAnnotation);
                            com.youthlin.mvc.annotation.Method[] urlMethod = (com.youthlin.mvc.annotation.Method[])
                                    AnnotationUtil.getValue(method, urlAnnotation, "method");
                            urlPrefix = urlPrefix == null ? "" : urlPrefix;
                            if (!urlPrefix.startsWith(FORWARD)) {
                                urlPrefix = FORWARD + urlPrefix;
                            }
                            if (urlPrefix.endsWith(FORWARD)) {
                                urlPrefix = urlPrefix.substring(0, urlPrefix.length() - FORWARD.length());
                            }
                            if (!url.startsWith(FORWARD)) {
                                url = FORWARD + url;
                            }
                            url = urlPrefix + url;
                            URLAndMethods urlAndMethods = new URLAndMethods(url, urlMethod);
                            ControllerAndMethod controllerAndMethod = new ControllerAndMethod(bean, method);
                            urlMapping.put(urlAndMethods, controllerAndMethod);
                            LOGGER.info("mapping url {} {} to method {}", url, Arrays.toString(urlMethod), method);
                        }
                    }
                }
            }
        }
        sce.getServletContext().setAttribute(URL_MAPPING_MAP, urlMapping);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.debug("contextDestroyed, sce = {}", sce);
    }

}
