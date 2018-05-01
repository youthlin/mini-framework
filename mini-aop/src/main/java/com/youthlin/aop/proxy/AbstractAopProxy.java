package com.youthlin.aop.proxy;

import com.youthlin.aop.core.AbstractAdvice;
import com.youthlin.aop.util.AopUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 14:49
 */
public abstract class AbstractAopProxy implements AopProxy {
    protected Object original;
    protected Class<?>[] itfs;

    private List<AbstractAdvice> adviceList = new ArrayList<>();

    AbstractAopProxy(Object original, Class<?>[] itfs) {
        this.original = original;
        this.itfs = itfs;
    }

    public abstract Object getProxy();

    @Override
    public Object getOriginal() {
        return original;
    }

    public void addAdvice(AbstractAdvice advice) {
        adviceList.add(advice);
    }

    protected boolean accept(Object advisor, Object original, Method method, Object[] args) {
        for (AbstractAdvice advice : adviceList) {
            return AopUtil.match(advice.getExpression(), method, advisor, original, args);
        }
        return false;
    }

}
