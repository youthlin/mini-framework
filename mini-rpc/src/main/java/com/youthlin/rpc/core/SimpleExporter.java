package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ProviderConfig;
import com.youthlin.rpc.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:45
 */
public class SimpleExporter implements Exporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExporter.class);
    private Map<Class<?>, Object> instanceMap = new HashMap<>();
    private Map<Key, ServerSocket> serverSocketMap = new HashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private static class Key {
        private String host;
        private int port;

        private Key(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (port != key.port) return false;
            return host != null ? host.equals(key.host) : key.host == null;
        }

        @Override
        public int hashCode() {
            int result = host != null ? host.hashCode() : 0;
            result = 31 * result + port;
            return result;
        }
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
                serverSocket = new ServerSocket(port);
                final ServerSocket ss = serverSocket;
                String hostAddress = serverSocket.getInetAddress().getHostAddress();
                LOGGER.info("export service at: {}:{} config host:{}", hostAddress, port, host);
                executorService.submit(new Runnable() {
                    @SuppressWarnings("InfiniteLoopStatement")
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                LOGGER.debug("waiting client...");
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
                LOGGER.debug("read from client...");
                Invocation invocation = (Invocation) in.readObject();
                LOGGER.debug("read from client: {}", invocation);
                invocation = handler(invocation);
                LOGGER.debug("after invoke: {}", invocation);
                out.writeObject(invocation);
            } catch (IOException e) {
                LOGGER.warn("Read from client: IOException", e);
            } catch (ClassNotFoundException | ClassCastException e) {
                e.printStackTrace();
            } finally {
                LOGGER.debug("closing client... {}", socket);
                NetUtil.close(in, out, socket);
            }
        }
    }

    @Override
    public Invocation handler(Invocation invocation) {
        Class<?> invokeInterface = invocation.invokeInterface();
        String methodName = invocation.methodName();
        Class<?>[] argsType = invocation.argsType();
        Object[] args = invocation.args();
        Object instance = instanceMap.get(invokeInterface);
        Class<?> instanceClass = instance.getClass();
        Method[] methods = instanceClass.getMethods();
        AbstractInvocationAdapter result = AbstractInvocationAdapter.newInvocation();
        result.setUid(invocation.uid());
        boolean found = false;
        try {
            for (Method method : methods) {
                if (Objects.equals(methodName, method.getName()) && Arrays.equals(argsType, method.getParameterTypes())) {
                    found = true;
                    Object invoke = method.invoke(instance, args);
                    result.setValue(invoke);
                    break;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Invoke Error", e);
            result.setException(e);
        }
        if (!found) {
            result.setException(new NoSuchMethodException(methodName));
        }
        return result;
    }

}
