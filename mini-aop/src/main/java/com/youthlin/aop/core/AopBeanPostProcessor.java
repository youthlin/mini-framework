package com.youthlin.aop.core;

import com.youthlin.aop.annotation.Aop;
import com.youthlin.aop.proxy.AopProxy;
import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.ioc.context.BeanPostProcessor;
import com.youthlin.ioc.context.Context;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private static final PointcutParameter[] EMPTY_POINTCUT_PARAMETER = new PointcutParameter[0];
    private static final Class[] POINTCUT_ANNOTATIONS = new Class[]{After.class, AfterReturning.class, AfterThrowing.class, Around.class, Before.class/*, Pointcut.class*/};
    private final Context context;
    private Set<Object> advisors;

    private static final Set<PointcutPrimitive> SUPPORT_POINTCUT_PRIMITIVE;

    static {
        //Set<PointcutPrimitive> allSupportedPointcutPrimitives = PointcutParser.getAllSupportedPointcutPrimitives();
        Set<PointcutPrimitive> tmp = new HashSet<>();
        tmp.add(PointcutPrimitive.EXECUTION);
        tmp.add(PointcutPrimitive.REFERENCE);
        SUPPORT_POINTCUT_PRIMITIVE = Collections.unmodifiableSet(tmp);
    }

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
        PointcutParser pointcutParser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(SUPPORT_POINTCUT_PRIMITIVE);
        for (Method method : advisor.getClass().getDeclaredMethods()) {
            Annotation annotation = AnnotationUtil.hasAnnotation(method, POINTCUT_ANNOTATIONS);
            if (annotation != null) {
                String expression = AnnotationUtil.getValue(method, annotation);
                PointcutExpression pointcutExpression;
                try {
                    pointcutExpression = pointcutParser.parsePointcutExpression(expression, advisor.getClass(), EMPTY_POINTCUT_PARAMETER);
                } catch (Exception e) {
                    LOGGER.error("Can not parse PointcutExpression:" + expression, e);
                    throw e;
                }
                if (!pointcutExpression.couldMatchJoinPointsInType(beanClass)) {
                    continue;
                }
                for (Method beanMethod : beanClass.getDeclaredMethods()) {
                    ShadowMatch shadowMatch = pointcutExpression.matchesMethodExecution(beanMethod);
                    if (shadowMatch.alwaysMatches()) {
                        return true;
                    }
                    if (shadowMatch.neverMatches()) {
                        continue;
                    }
                    if (shadowMatch.maybeMatches()) {
                        JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(null, bean, null);
                        if (joinPointMatch.matches()) {
                            return true;
                        }
                    }
                }
            }
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
