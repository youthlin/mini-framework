package com.youthlin.aop.core;

import com.youthlin.aop.advice.AbstractAdvice;
import com.youthlin.aop.advice.AfterAdvice;
import com.youthlin.aop.advice.AfterReturningAdvice;
import com.youthlin.aop.advice.AfterThrowingAdvice;
import com.youthlin.aop.advice.AroundAdvice;
import com.youthlin.aop.advice.BeforeAdvice;
import com.youthlin.aop.proxy.AbstractAopProxy;
import com.youthlin.aop.proxy.AopCglibProxy;
import com.youthlin.aop.proxy.AopJavaProxy;
import com.youthlin.aop.proxy.AopProxy;
import com.youthlin.aop.util.AopUtil;
import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.ioc.context.BeanPostProcessor;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.util.ClassUtil;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Advisor: 含有 {@link Aspect} 注解的类
 * JoinPoint: 连接点 如某个方法
 * PointCut： 连接点的集合 用 aspect execution 表达式表示
 * Advice: Advisor 里的切面代码
 * 一个 bean 可被多个 advice 织入
 * 创建: youthlin.chen
 * 时间: 2018-04-26 13:14
 */
public class AopBeanPostProcessor implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AopBeanPostProcessor.class);
    private static final Set<PointcutPrimitive> SUPPORT_POINTCUT_PRIMITIVE;
    private Map<Object, AbstractAopProxy> beanToProxyFactory = new HashMap<>();

    static {
        //Set<PointcutPrimitive> allSupportedPointcutPrimitives = PointcutParser.getAllSupportedPointcutPrimitives();
        Set<PointcutPrimitive> tmp = new HashSet<>();
        tmp.add(PointcutPrimitive.EXECUTION);
        tmp.add(PointcutPrimitive.REFERENCE);
        SUPPORT_POINTCUT_PRIMITIVE = Collections.unmodifiableSet(tmp);
    }

    private static final PointcutParser POINTCUT_PARSER = PointcutParser
            .getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(SUPPORT_POINTCUT_PRIMITIVE);
    private static final PointcutParameter[] EMPTY_POINTCUT_PARAMETER = new PointcutParameter[0];
    private static final Class[] POINTCUT_ANNOTATIONS = new Class[]{After.class, AfterReturning.class, AfterThrowing.class, Around.class, Before.class};

    private final Context context;
    private Set<Object> advisors;

    public AopBeanPostProcessor(Context context) {
        this.context = context;
    }

    @Override
    public Object postProcess(Object bean, Class beanClass, String beanName) {
        for (Object advisor : getAdvisors()) {
            bean = processAdvisor(advisor, bean, beanClass);
        }
        return bean;
    }

    /**
     * 返回容器中所有被 {@link Aspect} 注解的bean
     */
    private Set<Object> getAdvisors() {
        if (advisors == null) {
            advisors = new HashSet<>();
            for (Map.Entry<Class, Object> entry : context.getClazzBeanMap().entrySet()) {
                Class key = entry.getKey();
                if (AnnotationUtil.getAnnotation(key, Aspect.class) != null) {
                    advisors.add(entry.getValue());
                }
            }
        }
        return advisors;
    }

    private Object processAdvisor(Object advisor, Object bean, Class beanClass) {
        for (Method advisorMethod : advisor.getClass().getDeclaredMethods()) {
            Annotation annotation = AnnotationUtil.hasAnnotation(advisorMethod, POINTCUT_ANNOTATIONS);
            if (annotation != null) {
                String expression = AnnotationUtil.getValue(advisorMethod, annotation);
                PointcutExpression pointcutExpression;
                try {
                    pointcutExpression = POINTCUT_PARSER.parsePointcutExpression(expression, advisor.getClass(), EMPTY_POINTCUT_PARAMETER);
                } catch (Exception e) {
                    LOGGER.error("Can not parse PointcutExpression:" + expression, e);
                    throw e;
                }
                if (!pointcutExpression.couldMatchJoinPointsInType(beanClass)) {
                    continue;
                }
                for (Method beanMethod : beanClass.getDeclaredMethods()) {
                    if (AopUtil.match(pointcutExpression, beanMethod, advisor, bean, null)) {
                        bean = processAdvice(pointcutExpression, advisor, advisorMethod, bean, annotation.getClass(), beanClass);
                    }
                }
            }
        }
        return bean;
    }

    private Object processAdvice(PointcutExpression expression, Object advisor, Method adviceMethod, Object bean,
            Class<? extends Annotation> annotationClass, Class beanClass) {
        AbstractAdvice advice;
        if (Before.class.isAssignableFrom(annotationClass)) {
            advice = new BeforeAdvice();
        } else if (After.class.isAssignableFrom(annotationClass)) {
            advice = new AfterAdvice();
        } else if (AfterReturning.class.isAssignableFrom(annotationClass)) {
            advice = new AfterReturningAdvice();
        } else if (AfterThrowing.class.isAssignableFrom(annotationClass)) {
            advice = new AfterThrowingAdvice();
        } else if (Around.class.isAssignableFrom(annotationClass)) {
            advice = new AroundAdvice();
        } else {
            throw new IllegalStateException("Expected one of " + Arrays.toString(POINTCUT_ANNOTATIONS));
        }
        advice.setAdvisor(advisor);
        advice.setExpression(expression);
        advice.setAdvisorMethod(adviceMethod);
        AbstractAopProxy aopProxy = beanToProxyFactory.get(bean);
        if (aopProxy != null) {
            LOGGER.info("is aop proxy add advice");
            aopProxy.addAdvice(advice);
            return bean;
        }
        Class<?>[] itfs = ClassUtil.getAllInterfacesForClass(beanClass);
        int length = itfs.length;
        Class<?>[] itfsUse = new Class<?>[length + 1];
        System.arraycopy(itfs, 0, itfsUse, 0, length);
        itfsUse[length] = AopProxy.class;
        if (length > 0) {
            aopProxy = new AopJavaProxy(bean, itfsUse);
        } else {
            aopProxy = new AopCglibProxy(bean, itfsUse);
        }
        aopProxy.addAdvice(advice);
        bean = aopProxy.getProxy();
        beanToProxyFactory.put(bean, aopProxy);
        LOGGER.debug("bean class={},itfs={}", bean.getClass(), Arrays.toString(itfsUse));
        return bean;
    }


}
