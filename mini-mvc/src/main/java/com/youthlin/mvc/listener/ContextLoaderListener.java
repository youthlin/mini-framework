package com.youthlin.mvc.listener;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.Controller;
import com.youthlin.ioc.context.ClasspathContext;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.context.PreScanner;
import com.youthlin.mvc.annotation.HttpMethod;
import com.youthlin.mvc.annotation.URL;
import com.youthlin.mvc.servlet.Constants;
import com.youthlin.mvc.support.mybatis.MapperScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 13:20.
 */
@SuppressWarnings("WeakerAccess")
public class ContextLoaderListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextLoaderListener.class);
    private Context container;
    private Map<URLAndMethods, ControllerAndMethod> urlMapping = new ConcurrentHashMap<>();
    private Set<String> mappedUrls = new ConcurrentSkipListSet<>();

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
        servletContext.setAttribute(Constants.VIEW_PREFIX, "");
        servletContext.setAttribute(Constants.VIEW_SUFFIX, "");
        Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String parameterName = initParameterNames.nextElement();
            String initParameterValue = servletContext.getInitParameter(parameterName);
            if (parameterName.equals(Constants.VIEW_PREFIX_PARAM_NAME)) {
                servletContext.setAttribute(Constants.VIEW_PREFIX, initParameterValue);
            } else if (parameterName.equals(Constants.VIEW_SUFFIX_PARAM_NAME)) {
                servletContext.setAttribute(Constants.VIEW_SUFFIX, initParameterValue);
            }
            LOGGER.info("find initParameter: name = {}, value = {}", parameterName, initParameterValue);
        }
        String scan = servletContext.getInitParameter("scan");
        String[] scanPackages = { "" };
        if (scan != null) {
            scanPackages = scan.split("\\s|,|;");
        }
        container = new ClasspathContext(new PreScanner() {
            @Override public void preScan(Context context) {
                try {
                    MapperScanner mapperScanner = new MapperScanner();
                    mapperScanner.scan(context);
                } catch (Throwable e) {
                    LOGGER.debug("", e);
                }
            }
        }, scanPackages);
        LOGGER.info("register {} beans.", container.getBeanCount());
        servletContext.setAttribute(Constants.CONTAINER, container);
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
                urlPrefix = urlPrefix == null ? "" : urlPrefix;
                if (!urlPrefix.startsWith(Constants.FORWARD_CHAR)) {
                    urlPrefix = Constants.FORWARD_CHAR + urlPrefix;
                }
                if (urlPrefix.endsWith(Constants.FORWARD_CHAR)) {
                    urlPrefix = urlPrefix.substring(0, urlPrefix.length() - Constants.FORWARD_CHAR.length());
                }
                urlPrefix = sce.getServletContext().getContextPath() + urlPrefix;
                Method[] methods = beanClass.getMethods();
                if (methods != null) {
                    for (Method method : methods) {
                        if (method.getModifiers() != Modifier.PUBLIC) {
                            continue;
                        }
                        URL urlAnnotation = AnnotationUtil.getAnnotation(method, URL.class);
                        if (urlAnnotation != null) {
                            String url = (String) AnnotationUtil.getValue(method, urlAnnotation);
                            HttpMethod[] urlHttpMethod = (HttpMethod[])
                                    AnnotationUtil.getValue(method, urlAnnotation, "method");
                            if (!url.startsWith(Constants.FORWARD_CHAR)) {
                                url = Constants.FORWARD_CHAR + url;
                            }
                            url = urlPrefix + url;
                            URLAndMethods urlAndMethods = new URLAndMethods(url, urlHttpMethod);
                            ControllerAndMethod controllerAndMethod = new ControllerAndMethod(bean, method);
                            urlMapping.put(urlAndMethods, controllerAndMethod);
                            mappedUrls.add(url);
                            LOGGER.info("mapping url {} {} to method {}", url, Arrays.toString(urlHttpMethod), method);
                        }
                    }
                }
            }
        }
        sce.getServletContext().setAttribute(Constants.URL_MAPPING_MAP, urlMapping);
        sce.getServletContext().setAttribute(Constants.MAPPED_URL_SET, mappedUrls);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println(sce);
        for (Object bean : container.getBeans()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                PreDestroy preDestroy = AnnotationUtil.getAnnotation(method, PreDestroy.class);
                if (preDestroy != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] parameters = container.getBeans(parameterTypes);
                    try {
                        method.invoke(bean, parameters);
                    } catch (ReflectiveOperationException e) {
                        LOGGER.error("Error occurs when invoke PreDestroy method {} of bean {}", method, bean);
                    }
                    break;
                }
            }
        }
    }

}
