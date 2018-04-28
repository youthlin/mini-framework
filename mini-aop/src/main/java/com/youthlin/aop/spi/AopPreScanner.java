package com.youthlin.aop.spi;

import com.youthlin.aop.core.AopBeanPostProcessor;
import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPreScanner;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 将注有 {@link Aspect} 的类注册到容器中
 * 创建: youthlin.chen
 * 时间: 2018-04-26 13:16
 */
public class AopPreScanner implements IPreScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AopPreScanner.class);

    @Override
    public void preScan(Context context) {
        AopBeanPostProcessor aopBeanPostProcessor = new AopBeanPostProcessor(context);
        context.registerBean(aopBeanPostProcessor);
        String[] scanPackages = context.getScanPackages();
        Set<String> classNames = new HashSet<>(AnnotationUtil.getClassNames(scanPackages));
        for (String className : classNames) {
            try {
                Class<?> aClass = Class.forName(className);
                if (aClass.isAnnotation()) {
                    continue;
                }
                Aspect aop = AnnotationUtil.getAnnotation(aClass, Aspect.class);
                if (aop == null) {
                    continue;
                }
                if (AnnotationUtil.shouldNewInstance(aClass)) {
                    String beanName = AnnotationUtil.getBeanName(aClass);
                    Object instance = aClass.newInstance();
                    context.registerBean(instance, beanName);
                }
            } catch (Exception e) {
                LOGGER.warn("Error while AOP pre scan", e);
            }
        }
    }
}
