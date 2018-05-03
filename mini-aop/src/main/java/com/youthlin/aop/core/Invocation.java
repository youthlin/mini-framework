package com.youthlin.aop.core;

import com.youthlin.aop.advice.AbstractAdvice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 一次调用
 * 创建: youthlin.chen
 * 时间: 2018-05-01 15:19
 */
public class Invocation {
    private Object originalObject;
    private Method originalMethod;
    private Object[] args;
    private Object result;
    private Throwable throwable;
    private List<AbstractAdvice> adviceList;
    private int current;
    private boolean invoked = false;

    public Object invoke(JoinPointImpl pjp) throws Throwable {
        if (current < adviceList.size()) {
            adviceList.get(current++).invoke(pjp, this);
        }
        if (invoked) {
            if (getThrowable() != null) {
                throw getThrowable();
            }
            return getResult();
        }
        invoked = true;
        Throwable t = null;
        try {
            originalMethod.setAccessible(true);
            Object invoke = originalMethod.invoke(originalObject, args);
            setResult(invoke);
        } catch (IllegalAccessException e) {
            t = e;
        } catch (InvocationTargetException e) {
            t = e.getTargetException();
        }
        if (t != null) {
            setThrowable(t);
            throw t;
        }
        return getResult();
    }

    //region getter setter
    public Object getOriginalObject() {
        return originalObject;
    }

    public void setOriginalObject(Object originalObject) {
        this.originalObject = originalObject;
    }

    public Method getOriginalMethod() {
        return originalMethod;
    }

    public void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public List<AbstractAdvice> getAdviceList() {
        return adviceList;
    }

    public void setAdviceList(List<AbstractAdvice> adviceList) {
        this.adviceList = adviceList;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
    //endregion getter setter

}
