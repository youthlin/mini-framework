package com.youthlin.aop.core;

/**
 * 连接点 可以 invoke 被代理的方法
 * <p>
 * 创建: youthlin.chen
 * 时间: 2018-01-29 19:27
 */
public interface ProceedingJoinPoint extends JoinPoint {
    /**
     * 使用参数 args 调用方法
     */
    Object proceed(Object[] args) throws Throwable;

}
