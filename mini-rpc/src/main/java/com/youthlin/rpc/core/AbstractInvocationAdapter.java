package com.youthlin.rpc.core;

import java.util.Arrays;
import java.util.UUID;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:44
 */
public abstract class AbstractInvocationAdapter implements Invocation {
    private String uid = UUID.randomUUID().toString();
    private Class<?> invokeInterface;
    private Class<?> returnType;
    private String methodName;
    private Class<?>[] argsType;
    private Object[] args;
    private Object value;
    private Throwable exception;

    public static AbstractInvocationAdapter newInvocation() {
        return new Holder();
    }

    private static class Holder extends AbstractInvocationAdapter {
    }

    @Override
    public String uid() {
        return uid;
    }


    @Override
    public Class<?> invokeInterface() {
        return invokeInterface;
    }

    @Override
    public Class<?> returnType() {
        return returnType;
    }

    @Override
    public String methodName() {
        return methodName;
    }

    @Override
    public Class<?>[] argsType() {
        return argsType;
    }

    @Override
    public Object[] args() {
        return args;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    public AbstractInvocationAdapter setUid(String uid) {
        this.uid = uid;
        return this;
    }


    public AbstractInvocationAdapter setInvokeInterface(Class<?> invokeInterface) {
        this.invokeInterface = invokeInterface;
        return this;
    }

    public AbstractInvocationAdapter setReturnType(Class<?> returnType) {
        this.returnType = returnType;
        return this;
    }

    public AbstractInvocationAdapter setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public AbstractInvocationAdapter setArgsType(Class<?>[] argsType) {
        this.argsType = argsType;
        return this;
    }

    public AbstractInvocationAdapter setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public AbstractInvocationAdapter setValue(Object value) {
        this.value = value;
        return this;
    }

    public AbstractInvocationAdapter setException(Throwable exception) {
        this.exception = exception;
        return this;
    }

    @Override
    public String toString() {
        return "AbstractInvocationAdapter{" +
                "uid='" + uid + '\'' +
                ", invokeInterface=" + invokeInterface +
                ", returnType=" + returnType +
                ", methodName='" + methodName + '\'' +
                ", argsType=" + Arrays.toString(argsType) +
                ", args=" + Arrays.toString(args) +
                ", value=" + value +
                ", exception=" + exception +
                '}';
    }
}
