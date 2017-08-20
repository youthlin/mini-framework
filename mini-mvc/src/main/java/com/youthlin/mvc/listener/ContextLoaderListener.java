package com.youthlin.mvc.listener;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.Controller;
import com.youthlin.ioc.context.ClasspathContext;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPreScanner;
import com.youthlin.mvc.annotation.HttpMethod;
import com.youthlin.mvc.annotation.URL;
import com.youthlin.mvc.servlet.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
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
    private Map<URLAndMethod, ControllerAndMethod> urlMapping = new ConcurrentHashMap<>();
    private Set<String> mappedUrls = new ConcurrentSkipListSet<>();
    private static Context CONTAINER;

    public static Context getContext() {
        return CONTAINER;
    }

    /**
     * 容器启动时自动执行
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        init(sce);
        mapping(sce);
    }

    //初始化容器
    private void init(ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();
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
        String[] scanPackages = {""};
        if (scan != null) {
            scanPackages = scan.split("\\s|,|;");
        }
        ServiceLoader<IPreScanner> preScanners = ServiceLoader.load(IPreScanner.class);
        List<IPreScanner> preScannerList = new ArrayList<>();
        preScannerList.add(new IPreScanner() {
            @Override
            public void preScan(Context context) {
                context.registerBean(servletContext);
            }
        });
        for (IPreScanner preScanner : preScanners) {
            preScannerList.add(preScanner);
        }
        CONTAINER = new ClasspathContext(preScannerList, scanPackages);
        LOGGER.info("register {} beans.", CONTAINER.getBeanCount());
        servletContext.setAttribute(Constants.CONTAINER, CONTAINER);
    }

    //映射 URL 到 Controller 方法
    private void mapping(ServletContextEvent sce) {
        for (Object bean : CONTAINER.getBeans()) {
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
                        ControllerAndMethod controllerAndMethod = new ControllerAndMethod(bean, method);
                        URL urlAnnotation = AnnotationUtil.getAnnotation(method, URL.class);
                        if (urlAnnotation != null) {
                            HttpMethod[] urlHttpMethods = (HttpMethod[])
                                    AnnotationUtil.getValue(method, urlAnnotation, "method");
                            if (urlHttpMethods == null) {
                                continue;
                            }
                            String[] urls = (String[]) AnnotationUtil.getValue(method, urlAnnotation);
                            if (urls.length > 0) {
                                for (String url : urls) {
                                    if (!url.startsWith(Constants.FORWARD_CHAR)) {
                                        url = Constants.FORWARD_CHAR + url;
                                    }
                                    url = urlPrefix + url;
                                    if (urlHttpMethods.length == 0) {
                                        URLAndMethod urlAndMethod = new URLAndMethod(url);//for all methods
                                        urlMapping.put(urlAndMethod, controllerAndMethod);
                                        mappedUrls.add(url);
                                    }
                                    for (HttpMethod urlHttpMethod : urlHttpMethods) {
                                        URLAndMethod urlAndMethod = new URLAndMethod(url, urlHttpMethod);
                                        urlMapping.put(urlAndMethod, controllerAndMethod);
                                        mappedUrls.add(url);
                                    }
                                }
                            }
                            LOGGER.info("mapping url {} {} to method {}",
                                    Arrays.toString(urls), Arrays.toString(urlHttpMethods), method);
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
        // DispatcherServlet 在销毁时应当已经销毁了；
        // 但若没有访问DispatcherServlet那么其实他是不会init的
        // 也就不会销毁，所以这里也调一下
        preDestroy();
    }

    public static void preDestroy() {
        if (CONTAINER == null) {
            return;
        }
        for (Object bean : CONTAINER.getBeans()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                PreDestroy preDestroy = AnnotationUtil.getAnnotation(method, PreDestroy.class);
                if (preDestroy != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] parameters = CONTAINER.getBeans(parameterTypes);
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
