package com.youthlin.aop.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 17:46
 */
public class BeforeAdvice extends AdviceAdapter {

    @Override
    protected void before(JoinPointImpl pjp) throws Throwable {
        Method adviceMethod = pjp.getAdviceMethod();
        Class<?>[] parameterTypes = adviceMethod.getParameterTypes();
        Object[] arg = EMPTY_ARRAY;
        switch (parameterTypes.length) {
            case 1:
                arg = new Object[]{pjp};
            case 0:
                try {
                    adviceMethod.setAccessible(true);
                    adviceMethod.invoke(pjp.getAdvisor(), arg);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            default:
                throw new IllegalArgumentException("Before advice only accepts 0 or 1(JoinPoint) params:" + adviceMethod);
        }
    }
}
