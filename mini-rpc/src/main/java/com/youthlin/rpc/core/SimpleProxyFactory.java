package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.ProviderConfig;
import com.youthlin.rpc.core.config.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 17:38
 */
public class SimpleProxyFactory implements ProxyFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProxyFactory.class);
    private Class<?> interfaceType;
    private RegistryConfig registryConfig;
    private ConsumerConfig consumerConfig;
    private Map<Class<? extends Registry>, Registry> cache = new ConcurrentHashMap<>();

    private SimpleProxyFactory(Class<?> interfaceType, RegistryConfig registryConfig, ConsumerConfig consumerConfig) {
        this.interfaceType = interfaceType;
        this.registryConfig = registryConfig;
        this.consumerConfig = consumerConfig;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T newProxy(Class<T> interfaceType, RegistryConfig registryConfig, ConsumerConfig consumerConfig) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class[]{interfaceType},
                new SimpleProxyFactory(interfaceType, registryConfig, consumerConfig));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (registryConfig == null) {

            }
            Class<? extends Registry> registryClass = registryConfig.impl();
            Registry registry = cache.get(registryClass);
            if (registry == null) {
                registry = registryClass.newInstance();
                cache.put(registryClass, registry);
            }
            ProviderConfig providerConfig = registry.lookup(consumerConfig, interfaceType);

            String host = providerConfig.host();
            int port = providerConfig.port();
            Socket socket = new Socket(host, port);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            AbstractInvocationAdapter invocation = AbstractInvocationAdapter.newInvocation()
                    .setInvokeInterface(interfaceType)
                    .setReturnType(method.getReturnType())
                    .setMethodName(method.getName())
                    .setArgsType(method.getParameterTypes())
                    .setArgs(args);
            out.writeObject(invocation);
            Invocation result = (Invocation) in.readObject();
            if (result.getException() != null) {
                throw result.getException();
            }
            return result.getValue();
        } catch (Throwable t) {
            LOGGER.error("invoke error", t);
            throw new RpcException(t);
        }
    }
}
