package com.youthlin.rpc.spi;

import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPreScanner;
import com.youthlin.rpc.annotation.Rpc;
import com.youthlin.rpc.core.ProxyFactory;
import com.youthlin.rpc.core.SimpleProxyFactory;
import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.NoConfig;
import com.youthlin.rpc.core.config.RegistryConfig;
import com.youthlin.rpc.core.config.ServiceConfig;
import com.youthlin.rpc.core.config.SimpleConsumerConfig;
import com.youthlin.rpc.core.config.SimpleRegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 在容器扫描之前, 对 @Rpc 的字段创建代理, 这样容器扫描后注入字段时就可以注入了.
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:41
 */
public class RpcPreScanner implements IPreScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcPreScanner.class);

    @Override
    public void preScan(Context context) {
        String[] scanPackages = context.getScanPackages();
        Set<String> classNames = AnnotationUtil.getClassNames(scanPackages);
        try {
            for (String className : classNames) {
                Class<?> aClass = AnnotationUtil.forName(className);
                if (aClass == null) {
                    continue;
                }
                Field[] fields = aClass.getDeclaredFields();
                for (Field field : fields) {
                    Rpc rpc = AnnotationUtil.getAnnotation(field, Rpc.class);
                    if (rpc == null) {
                        continue;
                    }
                    process(context, field, rpc);//找到 @Rpc 的字段
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Error when pre scan for RPC", t);
        }
    }

    private void process(Context context, Field field, Rpc rpc) throws InstantiationException, IllegalAccessException {
        Class<?> interfaceType = field.getType();
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException("@Rpc Field should be an interface. " + interfaceType);
        }

        ProxyFactory proxyFactory;
        ConsumerConfig consumerConfig;
        Class<? extends ServiceConfig> configClass = rpc.config();
        if (configClass.equals(NoConfig.class)) {
            proxyFactory = new SimpleProxyFactory();
            consumerConfig = new SimpleConsumerConfig();
        } else {
            if (!ConsumerConfig.class.isAssignableFrom(configClass)) {
                throw new IllegalArgumentException(
                        "config of @Rpc Field should be sub class of ConsumerConfig. " + configClass);
            }
            ServiceConfig serviceConfig = newInstance(configClass);
            consumerConfig = ConsumerConfig.class.cast(serviceConfig);
            proxyFactory = newInstance(consumerConfig.proxy());
        }
        Class<? extends RegistryConfig> registryConfigClass = rpc.registry();
        RegistryConfig registryConfig;
        if (registryConfigClass.equals(NoConfig.class)) {
            registryConfig = new SimpleRegistryConfig();
        } else {
            registryConfig = newInstance(registryConfigClass);
        }
        Object newProxy = proxyFactory.newProxy(interfaceType, registryConfig, consumerConfig);

        //注册到容器 扫描完注入字段时就能注入成功
        context.registerBean(newProxy);
    }

    private <T> T newInstance(Class<T> aClass) throws IllegalAccessException, InstantiationException {
        if (!AnnotationUtil.shouldNewInstance(aClass)) {
            throw new IllegalArgumentException(aClass + " should not be an interface or abstract");
        }
        return aClass.newInstance();

    }
}
