package com.youthlin.rpc.core;

import java.util.Arrays;
import java.util.UUID;

/**
 * 流式 set 方法, 允许其中某些字段是 null.
 * 调用时没有结果和异常信息, 返回时可以不返回接口的信息, 只返回结果或异常
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:44
 */
@SuppressWarnings({ "UnusedReturnValue", "WeakerAccess" })
public class SimpleInvocation implements Invocation {
    private String uid = UUID.randomUUID().toString();
    private Class<?> invokeInterface;
    private Class<?> returnType;
    private String methodName;
    private Class<?>[] argsType;
    private Object[] args;
    private Object value;
    private Throwable exception;

    public static SimpleInvocation newInvocation() {
        return new SimpleInvocation();
    }

    private SimpleInvocation() {
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

    public SimpleInvocation setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public SimpleInvocation setInvokeInterface(Class<?> invokeInterface) {
        this.invokeInterface = invokeInterface;
        return this;
    }

    public SimpleInvocation setReturnType(Class<?> returnType) {
        this.returnType = returnType;
        return this;
    }

    public SimpleInvocation setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public SimpleInvocation setArgsType(Class<?>[] argsType) {
        this.argsType = argsType;
        return this;
    }

    public SimpleInvocation setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public SimpleInvocation setValue(Object value) {
        this.value = value;
        return this;
    }

    public SimpleInvocation setException(Throwable exception) {
        this.exception = exception;
        return this;
    }

    @Override
    public String toString() {
        return "SimpleInvocation{" +
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
