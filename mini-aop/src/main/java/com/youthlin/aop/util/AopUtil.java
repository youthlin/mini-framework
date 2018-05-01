package com.youthlin.aop.util;

import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2018-05-01 14:47
 */
public class AopUtil {
    public static final Object[] EMPTY_ARRAY = new Object[0];

    public static boolean match(PointcutExpression expression, Method method, Object thisObject, Object targetObject,
            Object[] args) {
        ShadowMatch shadowMatch = expression.matchesMethodExecution(method);
        if (shadowMatch.neverMatches()) {
            return false;
        }
        if (shadowMatch.alwaysMatches()) {
            return true;
        }
        if (shadowMatch.maybeMatches()) {
            JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(thisObject, targetObject, args);
            return joinPointMatch.matches();
        }
        return false;
    }
}
