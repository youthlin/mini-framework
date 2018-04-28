package com.youthlin.aop.core;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 14:35
 */
public interface ProceededJoinPoint extends ProceedingJoinPoint {
    Object getResult();

    Throwable getThrowable();
}
