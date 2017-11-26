package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 17:38
 */
public class SimpleProxyFactory implements ProxyFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T newProxy(Class<T> interfaceType, ConsumerConfig consumerConfig) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class[] { interfaceType },
                new SimpleProxy(interfaceType, consumerConfig));
    }

    private static class SimpleProxy implements InvocationHandler {
        private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProxyFactory.class);
        private Class<?> interfaceType;
        private ConsumerConfig consumerConfig;

        private SimpleProxy(Class<?> interfaceType, ConsumerConfig consumerConfig) {
            this.interfaceType = interfaceType;
            this.consumerConfig = consumerConfig;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                String host = consumerConfig.host();
                int port = consumerConfig.port();
                Socket socket = new Socket(host, port);

                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                ObjectInputStream in = new ObjectInputStream(inputStream);

                SimpleInvocation invocation = SimpleInvocation.newInvocation()
                        .setInvokeInterface(interfaceType)
                        .setReturnType(method.getReturnType())
                        .setMethodName(method.getName())
                        .setArgsType(method.getParameterTypes())
                        .setArgs(args);

                out.writeObject(invocation);
                Invocation result = (Invocation) in.readObject();

                if (result.getException() != null) {
                    throw new RpcException(result.getException());
                }
                return result.getValue();
            } catch (Throwable t) {
                LOGGER.error("invoke error", t);
                throw new RpcException(t);
            }
        }

    }

}
