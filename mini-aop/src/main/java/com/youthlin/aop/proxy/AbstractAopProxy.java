package com.youthlin.aop.proxy;

import com.youthlin.aop.advice.AbstractAdvice;
import com.youthlin.aop.core.Invocation;
import com.youthlin.aop.core.JoinPointImpl;
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

    protected List<AbstractAdvice> getMatchedAdviceList(Method method, Object[] args) {
        List<AbstractAdvice> list = new ArrayList<>(adviceList.size());
        for (AbstractAdvice advice : adviceList) {
            if (AopUtil.match(advice.getExpression(), method, advice.getAdvisor(), getOriginal(), args)) {
                list.add(advice);
            }
        }
        return list;
    }

    protected Invocation buildInvocation(Method originalMethod, Object[] args, List<AbstractAdvice> matchedAdviceList) {
        Invocation invocation = new Invocation();
        invocation.setOriginalObject(getOriginal());
        invocation.setOriginalMethod(originalMethod);
        invocation.setArgs(args);
        invocation.setAdviceList(matchedAdviceList);
        return invocation;
    }

    protected JoinPointImpl buildPjp(Object[] args, List<AbstractAdvice> matchedAdviceList) {
        JoinPointImpl pjp = new JoinPointImpl();
        pjp.setProxy(getProxy());
        pjp.setTarget(getOriginal());
        pjp.setArgs(args);
        pjp.setAdviceList(matchedAdviceList);
        return pjp;
    }

}
