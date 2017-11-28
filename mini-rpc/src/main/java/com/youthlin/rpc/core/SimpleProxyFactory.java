package com.youthlin.rpc.core;

import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.rpc.core.config.Config;
import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.util.NetUtil;
import com.youthlin.rpc.util.RpcUtil;
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
import java.net.Socket;
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

    private static class SimpleProxy extends AbstractInvocationHandler implements InvocationHandler {
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
        public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            //扩展, consumerConfig 里可以有注册中心的信息, 先请求注册中心拿到 这次要调用的提供者的 host, port
            String host = consumerConfig.host();
            int port = consumerConfig.port();

            Socket socket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            boolean needReturn = RpcUtil.needReturn(consumerConfig, method);
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
                    return AnnotationUtil.getDefaultValueOf(returnType);//不需要返回结果
                }

                long timeout = RpcUtil.getTimeOut(consumerConfig, method);

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

                if (RpcUtil.async(consumerConfig, method)) {
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

        @SuppressWarnings("unchecked")
        private void getResult(ObjectInputStream in,
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
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            SimpleProxy that = (SimpleProxy) o;

            if (interfaceType != null ? !interfaceType.equals(that.interfaceType) : that.interfaceType != null)
                return false;
            return consumerConfig != null ? consumerConfig.equals(that.consumerConfig) : that.consumerConfig == null;
        }

        @Override
        public int hashCode() {
            int result = interfaceType != null ? interfaceType.hashCode() : 0;
            result = 31 * result + (consumerConfig != null ? consumerConfig.hashCode() : 0);
            return result;
        }
    }

}
