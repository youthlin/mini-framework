package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.Config;
import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用 JDK 动态代理, 代理远程接口的所有方法, 通过 Socket 发送调用信息到提供者并等待结果返回.
 * 创建: youthlin.chen
 * 时间: 2017-11-26 17:38
 */
public class SimpleProxyFactory implements ProxyFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> T newProxy(Class<T> interfaceType, ConsumerConfig consumerConfig) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class[]{interfaceType},
                new SimpleProxy(interfaceType, consumerConfig));
    }

    private static class SimpleProxy implements InvocationHandler {
        private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProxyFactory.class);
        private ExecutorService executorService = Executors.newCachedThreadPool();
        private Class<?> interfaceType;
        private ConsumerConfig consumerConfig;

        {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    LOGGER.info("shutting down....");
                    executorService.shutdown();
                    LOGGER.info("shutdown success.");
                }
            }));
        }

        private SimpleProxy(Class<?> interfaceType, ConsumerConfig consumerConfig) {
            this.interfaceType = interfaceType;
            this.consumerConfig = consumerConfig;
        }

        //todo timeout, callback
        @SuppressWarnings("unchecked")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                String host = consumerConfig.host();
                int port = consumerConfig.port();
                Socket socket = new Socket(host, port);
                LOGGER.debug("Connect to provider {}", socket);
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                final ObjectOutputStream out = new ObjectOutputStream(outputStream);
                final ObjectInputStream in = new ObjectInputStream(inputStream);

                Class<?> returnType = method.getReturnType();
                SimpleInvocation invocation = SimpleInvocation.newInvocation()
                        .setInvokeInterface(interfaceType)
                        .setReturnType(returnType)
                        .setMethodName(method.getName())
                        .setArgsType(method.getParameterTypes())
                        .setArgs(args);

                out.writeObject(invocation);
                Boolean async = consumerConfig.async(method);
                if (async == null) {
                    async = consumerConfig.getConfig(method, Config.ASYNC, false);
                    if (async == null) {
                        async = consumerConfig.getConfig(Config.ASYNC, false);
                    }
                }
                if (async) {
                    RpcFuture<?> rpcFuture = RpcFuture.getRpcFuture();
                    final FutureAdapter futureAdapter = new FutureAdapter<>();
                    rpcFuture.setFuture(futureAdapter);
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getResult(in, futureAdapter);
                            } catch (Throwable t) {
                                futureAdapter.setException(t);
                            }
                        }
                    });
                    return null;//立即返回
                }
                return getResult(in, null);
            } catch (Throwable t) {
                LOGGER.error("invoke error. host:{} port:{}", consumerConfig.host(), consumerConfig.port(), t);
                if (t instanceof RpcException) {
                    throw t;
                }
                throw new RpcException(t);
            }
        }

        @SuppressWarnings("unchecked")
        private Object getResult(ObjectInputStream in,
                FutureAdapter futureAdapter) {
            Invocation result;
            try {
                result = (Invocation) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                if (futureAdapter != null) {
                    futureAdapter.setException(e);
                }
                throw new RpcException(e);
            }
            if (result.getException() != null) {
                if (futureAdapter != null) {
                    futureAdapter.setException(result.getException());
                }
                throw new RpcException(result.getException());
            }
            Object value = result.getValue();
            if (futureAdapter != null) {
                futureAdapter.setValue(value);
            }
            return value;
        }

    }

}
