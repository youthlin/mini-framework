package com.youthlin.rpc.spi;

import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPostScanner;
import com.youthlin.rpc.annotation.Rpc;
import com.youthlin.rpc.core.Exporter;
import com.youthlin.rpc.core.SimpleExporter;
import com.youthlin.rpc.core.config.ProviderConfig;
import com.youthlin.rpc.core.config.ServiceConfig;
import com.youthlin.rpc.core.config.SimpleProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 在容器扫描完成后, 对 @Rpc 的类暴露服务并注册到注册中心.
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:39
 */
public class RpcPostScanner implements IPostScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcPostScanner.class);
    private static final SimpleProviderConfig SIMPLE_PROVIDER_CONFIG = new SimpleProviderConfig();

    @Override
    public void postScanner(Context context) {
        try {
            Map<Class, Object> clazzBeanMap = context.getClazzBeanMap();
            for (Map.Entry<Class, Object> entry : clazzBeanMap.entrySet()) {
                Class aClass = entry.getKey();
                Object instance = entry.getValue();
                Rpc rpc = AnnotationUtil.getAnnotation(aClass, Rpc.class);
                if (rpc == null) {
                    continue;
                }

                Class<? extends ServiceConfig> config = rpc.config();
                ProviderConfig providerConfig;
                Exporter exporterImpl;
                if (config.equals(ServiceConfig.class)) {//没有配置
                    providerConfig = SIMPLE_PROVIDER_CONFIG;
                    exporterImpl = SimpleExporter.INSTANCE;
                } else {
                    if (!ProviderConfig.class.isAssignableFrom(config)) {
                        LOGGER.warn("Service config should be a sub class of ProviderConfig on Provider Side. {}", rpc);
                        throw new IllegalArgumentException(
                                "Service config should be a sub class of ProviderConfig on Provider Side. " + rpc);
                    }
                    ServiceConfig serviceConfig = getFromContextOrNewInstance(context, config);
                    providerConfig = ProviderConfig.class.cast(serviceConfig);
                    Class<? extends Exporter> exporter = providerConfig.exporter();
                    exporterImpl = getFromContextOrNewInstance(context, exporter);
                }
                exporterImpl.export(providerConfig, instance);
            }
        } catch (Throwable t) {
            LOGGER.error("Error when post scan for RPC", t);
        }
    }

    private <T> T getFromContextOrNewInstance(Context context,
            Class<T> aClass) throws IllegalAccessException, InstantiationException {
        T bean = context.getBean(aClass);
        if (bean == null) {
            if (!AnnotationUtil.shouldNewInstance(aClass)) {
                throw new IllegalArgumentException(aClass + " should not be an interface or abstract");
            }
            bean = aClass.newInstance();
        }
        return bean;
    }
}
