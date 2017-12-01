package com.youthlin.rpc.core;

import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.rpc.core.config.Config;
import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.ProviderConfig;
import com.youthlin.rpc.core.config.SimpleConsumerConfig;
import com.youthlin.rpc.core.config.SimpleProviderConfig;
import com.youthlin.rpc.util.NetUtil;
import com.youthlin.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 使用 JDK 动态代理, 代理远程接口的所有方法, 通过 Socket 发送调用信息到提供者并等待结果返回.
 * 创建: youthlin.chen
 * 时间: 2017-11-26 17:38
 */
public class SimpleProxyFactory implements ProxyFactory {
    public static SimpleProxyFactory INSTANCE = new SimpleProxyFactory();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T newProxy(Class<T> interfaceType, ConsumerConfig consumerConfig) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class[] { interfaceType },
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
            Thread hook = new Thread(new Runnable() {
                @Override
                public void run() {
                    LOGGER.info("shutting down....");
                    executorService.shutdown();
                    LOGGER.info("shutdown success.");
                }
            });
            hook.setName("ProxyShutDownHook");
            Runtime.getRuntime().addShutdownHook(hook);
        }

        private SimpleProxy(Class<?> interfaceType, ConsumerConfig consumerConfig) {
            this.interfaceType = interfaceType;
            this.consumerConfig = consumerConfig;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            //扩展, consumerConfig 里可以有注册中心的信息, 先请求注册中心拿到这次要调用的提供者的 host, port
            String host = consumerConfig.host();
            int port = consumerConfig.port();

            Socket socket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            LinkedList<CallbackPair> list = null;
            boolean needReturn = RpcUtil.needReturn(consumerConfig, method);
            try {
                //t/o/d/o// use NIO
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
                SimpleInvocation invocation = SimpleInvocation.newInvocation();
                invocation.setInvokeInterface(interfaceType)
                        .setReturnType(returnType)
                        .setMethodName(method.getName())
                        .setArgsType(method.getParameterTypes())
                        .setArgs(processArgs(invocation, method, args))
                        .ext(Config.RETURN, needReturn);

                list = (LinkedList<CallbackPair>) invocation.ext().remove(Config.TEMP);
                final LinkedList<CallbackPair> flist = list;

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
                            unExport(flist);
                        }
                    }
                });

                if (RpcUtil.async(consumerConfig, method)) {
                    return AnnotationUtil.getDefaultValueOf(returnType);//立即返回
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
                    unExport(list);
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

        private Object[] processArgs(Invocation invocation, Method method, Object[] args) {
            if (method.getParameterTypes().length == 0) {
                return RpcUtil.EMPTY_ARRAY;
            }
            boolean[] callback = RpcUtil.callback(consumerConfig, method);
            ConsumerConfig[] consumerConfigs = new ConsumerConfig[method.getParameterTypes().length];
            if (callback != null) {
                invocation.ext().put(Config.CALLBACK, callback);
                for (int i = 0; i < callback.length; i++) {
                    if (callback[i]) {
                        ProviderConfig providerConfig = export(invocation, method, i, args[i]);
                        consumerConfigs[i] = new SimpleConsumerConfig()
                                .setPort(providerConfig.port())
                                .setHost(NetUtil.getLocalAddress().getHostAddress());
                        args[i] = null;
                    }
                }
            }
            invocation.ext().put(Config.CONSUMER_CONFIG, consumerConfigs);
            return args;
        }

        private static class CallbackPair implements Serializable {
            ProviderConfig providerConfig;
            Exporter exporter;
            Object instance;

            @Override
            public String toString() {
                return "CallbackPair{" +
                        "providerConfig=" + providerConfig +
                        ", exporter=" + exporter +
                        ", instance=" + instance +
                        '}';
            }
        }

        private ProviderConfig export(Invocation invocation, Method method, int index, Object arg) {
            LOGGER.debug("callback: {} {} {}", method, index, arg);
            ProviderConfig providerConfig = SimpleProviderConfig.INSTANCE;
            ProviderConfig[] providerConfigs = RpcUtil.providerConfigOfCallbackParameter(consumerConfig, method);
            if (providerConfigs != null) {
                providerConfig = providerConfigs[index];
            }
            Class<? extends Exporter> exporter = providerConfig.exporter();
            Exporter exporterImpl = SimpleExporter.INSTANCE;
            if (!exporter.equals(SimpleExporter.class)) {
                exporterImpl = AnnotationUtil.newInstance(exporter);
            }
            if (exporterImpl == null) {
                throw new IllegalArgumentException("Can not get exporter instance. " + exporter);
            }
            exporterImpl.export(providerConfig, arg);

            CallbackPair callbackPair = new CallbackPair();
            callbackPair.providerConfig = providerConfig;
            callbackPair.exporter = exporterImpl;
            callbackPair.instance = arg;
            @SuppressWarnings("unchecked")
            LinkedList<CallbackPair> list = (LinkedList<CallbackPair>) invocation.ext().get(Config.TEMP);
            if (list == null) {
                list = new LinkedList<>();
            }
            list.add(callbackPair);
            invocation.ext().put(Config.TEMP, list);

            LOGGER.debug("callback export at {}", providerConfig);
            return providerConfig;
        }

        private void unExport(List<CallbackPair> list) {
            if (list != null) {
                for (CallbackPair callbackPair : list) {
                    try {
                        callbackPair.exporter.unExport(callbackPair.providerConfig, callbackPair.instance,
                                consumerConfig.getConfig(Config.CALLBACK_TIMEOUT, 6000), TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        LOGGER.warn("Error when unExport callback service. {}", callbackPair, e);
                    }
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            SimpleProxy that = (SimpleProxy) o;

            //noinspection SimplifiableIfStatement
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
