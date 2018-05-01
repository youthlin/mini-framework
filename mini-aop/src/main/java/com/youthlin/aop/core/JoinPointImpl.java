package com.youthlin.aop.core;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 封装一次调用 包含参数调用后的结果异常信息等
 * 创建: youthlin.chen
 * 时间: 2018-04-28 14:31
 */
public class JoinPointImpl implements ProceededJoinPoint {
    private Object advisor;
    private Object target;
    private Method targetMethod;
    private Object[] args;
    private Object result;
    private Throwable throwable;
    private List<AbstractAdvice> adviceList;

    public Object getAdvisor() {
        return advisor;
    }

    public JoinPointImpl setAdvisor(Object advisor) {
        this.advisor = advisor;
        return this;
    }

    @Override
    public String toShortString() {
        return null;
    }

    @Override
    public String toLongString() {
        return null;
    }

    @Override
    public Object getThis() {
        return null;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    public JoinPointImpl setTarget(Object target) {
        this.target = target;
        return this;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public JoinPointImpl setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
        return this;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public Signature getSignature() {
        return null;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return null;
    }

    @Override
    public String getKind() {
        return null;
    }

    @Override
    public StaticPart getStaticPart() {
        return null;
    }

    public JoinPointImpl setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    @Override
    public Object getResult() {
        return result;
    }

    public JoinPointImpl setResult(Object result) {
        this.result = result;
        return this;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    public JoinPointImpl setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public List<AbstractAdvice> getAdviceList() {
        return adviceList;
    }

    public JoinPointImpl setAdviceList(List<AbstractAdvice> adviceList) {
        this.adviceList = adviceList;
        return this;
    }

    @Override
    public void set$AroundClosure(AroundClosure arc) {

    }

    @Override
    public Object proceed() throws Throwable {
        return null;
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return null;
    }
}
