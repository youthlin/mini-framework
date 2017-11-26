package com.youthlin.rpc.core;

import com.youthlin.rpc.core.config.ConsumerConfig;
import com.youthlin.rpc.core.config.ProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 16:19
 */
public class SimpleRegistry implements Registry {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRegistry.class);
    private Map<Class<?>, Object> serviceInstanceMap = new HashMap<>();
    private Map<Object, ProviderConfig> serviceConfigMap = new HashMap<>();

    @Override
    public void register(ProviderConfig providerConfig, Object serviceInstance) {
        LOGGER.debug("service register: {} {}", providerConfig, serviceInstance);
        Class<?>[] interfaces = providerConfig.interfaces();
        if (interfaces == null || interfaces.length == 0) {
            interfaces = serviceInstance.getClass().getInterfaces();
        }
        for (Class<?> anInterface : interfaces) {
            serviceInstanceMap.put(anInterface, serviceInstance);
        }
        serviceConfigMap.put(serviceInstance, providerConfig);
    }

    @Override
    public void unregister(ProviderConfig providerConfig, Object serviceInstance) {
        LOGGER.debug("service unregister: {} {}", providerConfig, serviceInstance);
        Class<?>[] interfaces = providerConfig.interfaces();
        if (interfaces == null || interfaces.length == 0) {
            interfaces = serviceInstance.getClass().getInterfaces();
        }
        for (Class<?> anInterface : interfaces) {
            Object o = serviceInstanceMap.get(anInterface);
            if (Objects.equals(serviceInstance, o)) {
                serviceInstanceMap.remove(anInterface);
            }
        }
        serviceConfigMap.remove(serviceInstance);
    }

    @Override
    public ProviderConfig lookup(ConsumerConfig consumerConfig, Class<?> serviceType) {
        LOGGER.debug("consumer lookup: {} {}", serviceType, consumerConfig);
        Object serviceInstance = serviceInstanceMap.get(serviceType);
        ProviderConfig providerConfig = serviceConfigMap.get(serviceInstance);
        LOGGER.debug("consumer got provider config: {}", providerConfig);
        return providerConfig;
    }

}
