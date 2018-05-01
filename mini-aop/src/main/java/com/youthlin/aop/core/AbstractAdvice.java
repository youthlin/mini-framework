package com.youthlin.aop.core;


import org.aspectj.weaver.tools.PointcutExpression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2018-05-01 14:46
 */
public class AbstractAdvice implements Advice {
    private Object advisor;
    private PointcutExpression expression;//用于判断是否match某个方法
    private Method advisorMethod;

    public Object getAdvisor() {
        return advisor;
    }

    public void setAdvisor(Object advisor) {
        this.advisor = advisor;
    }

    public PointcutExpression getExpression() {
        return expression;
    }

    public void setExpression(PointcutExpression expression) {
        this.expression = expression;
    }

    public Method getAdvisorMethod() {
        return advisorMethod;
    }

    public void setAdvisorMethod(Method advisorMethod) {
        this.advisorMethod = advisorMethod;
    }

    public void invoke(JoinPointImpl point, Invocation invocation) throws Throwable {
        before(point);
        try {
            doInvoke(point, invocation);
            after(point);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();
            }
            point.setThrowable(t);
            onException(point);
        } finally {
            onDone(point);
        }


    }

    protected void before(JoinPointImpl point) throws Throwable {
    }

    protected void doInvoke(JoinPointImpl point, Invocation invocation) throws Throwable {
        invocation.invoke(point);
    }

    protected void after(JoinPointImpl point) throws Throwable {
    }

    protected void onException(JoinPointImpl point) throws Throwable {
    }

    protected void onDone(JoinPointImpl point) throws Throwable {
    }
}
