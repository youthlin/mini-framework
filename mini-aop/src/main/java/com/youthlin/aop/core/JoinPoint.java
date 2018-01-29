package com.youthlin.aop.core;

/**
 * 连接点
 * <p>
 * 创建: youthlin.chen
 * 时间: 2018-01-29 19:26
 */
public interface JoinPoint {
    Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 获取代理类
     */
    Object getThis();

    /**
     * 获取原始被代理类
     */
    Object getTarget();

    /**
     * 获取参数 不会为空 没有参数时 返回{@link #EMPTY_OBJECT_ARRAY}
     */
    Object[] getArgs();

}
