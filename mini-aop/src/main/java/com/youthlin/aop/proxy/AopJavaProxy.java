package com.youthlin.aop.proxy;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-27 19:18
 */
public class AopJavaProxy implements AopProxy {
    private final Object original;

    public AopJavaProxy(Object original) {
        this.original = original;
    }

    @Override
    public Object getOriginal() {
        return original;
    }
}
