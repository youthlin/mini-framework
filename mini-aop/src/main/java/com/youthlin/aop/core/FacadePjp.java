package com.youthlin.aop.core;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

/**
 * 创建: youthlin.chen
 * 时间: 2018-05-03 19:03
 */
public class FacadePjp implements ProceededJoinPoint {
    private ProceededJoinPoint delegate;

    public FacadePjp(ProceededJoinPoint pjp) {
        delegate = pjp;
    }

    @Override
    public Object getResult() {
        return delegate.getResult();
    }

    @Override
    public Throwable getThrowable() {
        return delegate.getThrowable();
    }

    @Override
    public void set$AroundClosure(AroundClosure arc) {
        delegate.set$AroundClosure(arc);
    }

    @Override
    public Object proceed() throws Throwable {
        return delegate.proceed();
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return delegate.proceed(args);
    }

    @Override
    public String toShortString() {
        return delegate.toShortString();
    }

    @Override
    public String toLongString() {
        return delegate.toLongString();
    }

    @Override
    public Object getThis() {
        return delegate.getThis();
    }

    @Override
    public Object getTarget() {
        return delegate.getTarget();
    }

    @Override
    public Object[] getArgs() {
        return delegate.getArgs();
    }

    @Override
    public Signature getSignature() {
        return delegate.getSignature();
    }

    @Override
    public SourceLocation getSourceLocation() {
        return delegate.getSourceLocation();
    }

    @Override
    public String getKind() {
        return delegate.getKind();
    }

    @Override
    public StaticPart getStaticPart() {
        return delegate.getStaticPart();
    }
}
