package com.youthlin.aop.core;

import com.youthlin.aop.annotation.Aop;
import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.ioc.context.BeanPostProcessor;
import com.youthlin.ioc.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-26 13:14
 */
public class AopBeanPostProcessor implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AopBeanPostProcessor.class);
    private final Context context;
    private Set<Object> advisors;

    public AopBeanPostProcessor(Context context) {
        this.context = context;
    }

    @Override
    public Object postProcess(Object bean, String beanName) {
        for (Object advisor : getAdvisors()) {
            Class beanClass;
            if (bean instanceof AopProxy) {
                Object original = ((AopProxy) bean).getOriginal();
                beanClass = original.getClass();
            } else {
                beanClass = bean.getClass();
            }
            if (match(advisor, bean, beanClass)) {
                LOGGER.info("[AOP] {} match {}", bean, advisor);
                bean = createProxy(advisor, bean, beanClass);
            }
        }
        return bean;
    }

    private Set<Object> getAdvisors() {
        if (advisors == null) {
            advisors = new HashSet<>();
            for (Map.Entry<Class, Object> entry : context.getClazzBeanMap().entrySet()) {
                Class key = entry.getKey();
                if (AnnotationUtil.getAnnotation(key, Aop.class) != null) {
                    advisors.add(entry.getValue());
                }
            }
        }
        return advisors;
    }

    private boolean match(Object advisor, Object bean, Class beanClass) {
        if (beanClass.getName().toLowerCase().contains("hello")) {
            return true;
        }
        return false;
    }

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private Object createProxy(Object advisor, final Object bean, Class beanClass) {
        Class[] interfaces = beanClass.getInterfaces();
        if ((beanClass.isInterface() || interfaces.length > 0)
                && !(bean instanceof AopProxy)) {
            List<Class> itfsList = new ArrayList<>(Arrays.asList(interfaces));
            itfsList.add(AopProxy.class);
            return Proxy.newProxyInstance(getClass().getClassLoader(), itfsList.toArray(EMPTY_CLASS_ARRAY), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.toString().equals("")) {
                        return bean;
                    }
                    return method.invoke(bean, args);
                }
            });
        }
        return bean;
    }

}
