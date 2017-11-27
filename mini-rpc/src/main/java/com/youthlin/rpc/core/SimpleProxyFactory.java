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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    public static void setExecutoeService(ExecutorService service) {
        SimpleProxy.executorService = service;
    }

    private static class SimpleProxy implements InvocationHandler {
        private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProxyFactory.class);
        private static ExecutorService executorService = Executors.newCachedThreadPool();
        private Class<?> interfaceType;
        private ConsumerConfig consumerConfig;

        static {
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

        //todo  callback
        @SuppressWarnings("unchecked")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //扩展, consumerConfig 里可以有注册中心的信息, 先请求注册中心拿到 这次要调用的提供者的 host, port
            String host = consumerConfig.host();
            int port = consumerConfig.port();
            Key key = new Key(host, port);

            if (!method.getDeclaringClass().equals(interfaceType)) {//不是这个接口的方法
                LOGGER.debug("Method {} is not of remote interface {}. invoke local.", method, interfaceType);
                return method.invoke(key, args);//toString, hashCode, equals, etc...//fixme If it's Ok???
            }
            Socket socket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            boolean needReturn = needReturn(method);
            try {
                socket = new Socket(host, port);
                LOGGER.debug("Connect to provider {}", socket);
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                out = new ObjectOutputStream(outputStream);
                in = new ObjectInputStream(inputStream);
                final Socket fs = socket;
                final ObjectInputStream fin = in;
                final ObjectOutputStream fout = out;
                Class<?> returnType = method.getReturnType();
                SimpleInvocation invocation = SimpleInvocation.newInvocation()
                        .setInvokeInterface(interfaceType)
                        .setReturnType(returnType)
                        .setMethodName(method.getName())
                        .setArgsType(method.getParameterTypes())
                        .setArgs(args)
                        .ext(Config.RETURN, needReturn);

                out.writeObject(invocation);

                if (!needReturn) {
                    return null;//不需要返回结果
                }

                long timeout = getTimeOut(method);

                RpcFuture<?> rpcFuture = RpcFuture.getRpcFuture();
                final FutureAdapter futureAdapter = new FutureAdapter<>();
                rpcFuture.setFuture(futureAdapter);
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getResult(fin, futureAdapter);
                        } catch (Throwable t) {
                            futureAdapter.setException(t);
                        } finally {
                            NetUtil.close(fin, fout, fs);
                        }
                    }
                });

                if (async(method)) {
                    return null;//立即返回
                }
                return futureAdapter.get(timeout, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                LOGGER.error("invoke error. host:{} port:{}", consumerConfig.host(), consumerConfig.port(), t);
                if (t instanceof RpcException) {
                    throw t;
                }
                throw new RpcException(t);
            } finally {
                if (!needReturn) {
                    NetUtil.close(in, out, socket);
                }
            }
        }

        private long getTimeOut(Method method) {
            Long timeout = consumerConfig.timeout(method);
            if (timeout == null) {
                timeout = consumerConfig.getConfig(method, Config.TIMEOUT, Long.MAX_VALUE);
                if (timeout == null) {
                    timeout = consumerConfig.getConfig(Config.TIMEOUT, Long.MAX_VALUE);
                }
            }
            return timeout;
        }

        private boolean async(Method method) {
            Boolean async = consumerConfig.async(method);
            if (async == null) {
                async = consumerConfig.getConfig(method, Config.ASYNC, false);
                if (async == null) {
                    async = consumerConfig.getConfig(Config.ASYNC, false);
                }
            }
            return async;
        }

        private boolean needReturn(Method method) {
            Boolean needRet = consumerConfig.getConfig(method, Config.RETURN, true);
            if (needRet == null) {
                needRet = consumerConfig.getConfig(Config.RETURN, true);
            }
            return needRet;
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
