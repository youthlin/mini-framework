package com.youthlin.rpc.core;

import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.rpc.core.config.Config;
import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.ProviderConfig;
import com.youthlin.rpc.core.config.SimpleConsumerConfig;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:45
 */
public class SimpleExporter implements Exporter {
    public static final SimpleExporter INSTANCE = new SimpleExporter();
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExporter.class);
    private Map<Class<?>, Object> instanceMap = new ConcurrentHashMap<>();
    private Map<Key, ServerSocket> serverSocketMap = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("shutting down....");
                executorService.shutdown();
                for (Map.Entry<Key, ServerSocket> entry : serverSocketMap.entrySet()) {
                    ServerSocket serverSocket = entry.getValue();
                    NetUtil.close(serverSocket);
                    LOGGER.info("ServerSocket Closed {} {}", entry.getKey(), serverSocket);
                }
                LOGGER.info("shutdown success.");
            }
        }));
    }

    @Override
    public void export(ProviderConfig providerConfig, Object instance) {
        Class<?>[] interfaces = providerConfig.interfaces();
        if (interfaces == null || interfaces.length == 0) {
            interfaces = instance.getClass().getInterfaces();
        }
        for (Class<?> anInterface : interfaces) {
            instanceMap.put(anInterface, instance);
        }
        String host = providerConfig.host();
        int port = providerConfig.port();
        Key key = new Key(host, port);
        ServerSocket serverSocket = serverSocketMap.get(key);
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));
                final ServerSocket ss = serverSocket;
                String hostAddress = serverSocket.getInetAddress().getHostAddress();
                LOGGER.info("export service at: {}:{} config host:{}", hostAddress, port, host);
                executorService.submit(new Runnable() {
                    @SuppressWarnings("InfiniteLoopStatement")
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                if (ss.isClosed()) {
                                    break;
                                }
                                LOGGER.trace("waiting client...");
                                Socket client = ss.accept();
                                executorService.submit(new Handler(client));
                            } catch (IOException e) {
                                LOGGER.warn("Accept Client IOException", e);
                            }
                        }
                    }
                });

            } catch (IOException e) {
                LOGGER.warn("new ServerSocket IOException", e);
            }
            serverSocketMap.put(key, serverSocket);
        }
    }

    @Override
    public void unExport(final ProviderConfig providerConfig, final Object instance,
            final long delay, final TimeUnit unit) {
        LOGGER.debug("unExport service after {} {}: {}", delay, unit, instance.getClass());
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(unit.toMillis(delay));
                } catch (InterruptedException ignore) {
                }
                Class<?>[] interfaces = providerConfig.interfaces();
                if (interfaces == null || interfaces.length == 0) {
                    interfaces = instance.getClass().getInterfaces();
                }
                for (Class<?> anInterface : interfaces) {
                    instanceMap.remove(anInterface);
                }
                String host = providerConfig.host();
                int port = providerConfig.port();
                Key key = new Key(host, port);
                serverSocketMap.remove(key);
                LOGGER.debug("{} unExported", instance.getClass());
            }
        });
    }

    private class Handler implements Runnable {
        private Socket socket;

        private Handler(Socket client) {
            socket = client;
        }

        @Override
        public void run() {
            LOGGER.debug("new client: {}", socket);
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                in = new ObjectInputStream(inputStream);
                out = new ObjectOutputStream(outputStream);

                Invocation invocation = readInvocation(in);

                boolean needReturn = RpcUtil.needReturn(invocation);
                if (invocation.getException() != null) {
                    if (needReturn) {
                        writeInvocation(out, invocation);
                        return;
                    }
                }

                boolean debugEnabled = LOGGER.isDebugEnabled();
                if (debugEnabled) {
                    LOGGER.debug("read from client: {} {}", invocation.methodName(), invocation);
                }

                invocation = handler(invocation);

                if (debugEnabled) {
                    LOGGER.debug("after invoke: value={} ex={}", invocation.getValue(), invocation.getException());
                }

                if (needReturn) {
                    LOGGER.trace("return to client");
                    writeInvocation(out, invocation);
                }
            } catch (IOException e) {
                LOGGER.warn("Read from client: IOException", e);
            } catch (Throwable t) {
                LOGGER.error("Unhandled Exception", t);
            } finally {
                LOGGER.debug("closing client... {}", socket);
                NetUtil.close(out, in, socket);
            }
        }
    }

    private Invocation readInvocation(ObjectInputStream in) {
        Invocation invocation;
        try {
            LOGGER.trace("read from client...");
            invocation = (Invocation) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            LOGGER.error("Read Invocation Error", e);
            invocation = SimpleInvocation.newInvocation().setException(e);
        }
        return invocation;
    }

    private void writeInvocation(ObjectOutputStream out, Invocation invocation) throws IOException {
        out.writeObject(invocation);
    }

    @Override
    public Invocation handler(Invocation invocation) {
        Class<?> invokeInterface = invocation.invokeInterface();
        String methodName = invocation.methodName();
        Class<?>[] argsType = invocation.argsType();
        Object[] args = processArgs(invocation);

        Object instance = instanceMap.get(invokeInterface);
        Class<?> instanceClass = instance.getClass();
        Method[] methods = instanceClass.getMethods();
        SimpleInvocation result = SimpleInvocation.newInvocation();
        result.setUid(invocation.uid());
        boolean found = false;
        try {
            for (Method method : methods) {
                if (Objects.equals(methodName, method.getName()) && Arrays
                        .equals(argsType, method.getParameterTypes())) {
                    found = true;
                    method.setAccessible(true);
                    Object invoke = method.invoke(instance, args);
                    result.setValue(invoke);
                    break;
                }
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("Invoke Error", e);
            result.setException(e);
        } catch (InvocationTargetException e) {
            LOGGER.error("Invoke Error", e);
            result.setException(e.getCause());
        }
        if (!found) {
            result.setException(new NoSuchMethodException(methodName));
        }
        return result;
    }

    private Object[] processArgs(Invocation invocation) {
        Object[] args = invocation.args();
        Serializable callbackConfig = invocation.ext().get(Config.CALLBACK);
        boolean[] callback = (boolean[]) callbackConfig;
        if (callback != null) {
            Class<?>[] argsType = invocation.argsType();
            if (callback.length != argsType.length) {
                throw new IllegalArgumentException("callback config " + callback.length + " != " + argsType.length + " parameters");
            }
            for (int i = 0; i < callback.length; i++) {
                if (callback[i]) {
                    args[i] = refer(invocation, i);
                }
            }
        }
        return args;
    }

    private Object refer(Invocation invocation, int index) {
        Serializable serializable = invocation.ext().get(Config.CONSUMER_CONFIG);
        ConsumerConfig consumerConfig = ((ConsumerConfig[]) serializable)[index];
        LOGGER.debug("refer from {}:{}", consumerConfig.host(), consumerConfig.port());
        Class<? extends ProxyFactory> proxy = consumerConfig.proxy();
        ProxyFactory proxyImpl = SimpleProxyFactory.INSTANCE;
        if (!proxy.equals(SimpleProxyFactory.class)) {
            proxyImpl = AnnotationUtil.newInstance(proxy);
        }
        if (proxyImpl == null) {
            throw new IllegalArgumentException("Can not get ProxyFactory instance. " + proxy);
        }
        return proxyImpl.newProxy(invocation.argsType()[index], consumerConfig);
    }

}
