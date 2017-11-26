package com.youthlin.rpc.annotation;

import com.youthlin.rpc.core.config.ProxyConfig;
import com.youthlin.rpc.core.config.NotConfig;
import com.youthlin.rpc.core.config.RegistryConfig;
import com.youthlin.rpc.core.config.ServiceConfig;

import javax.annotation.Resource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解在类上, 表示该类是一个服务提供者;
 * 注解在字段上, 表示该字段引用的远程服务.
 * <p>
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:37
 */
@Resource
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rpc {
    /**
     * 注册中心
     */
    Class<? extends RegistryConfig> registry() default NotConfig.class;

    /**
     * 对于提供者: 这个配置管理本机对外的响应;
     * 对于消费者: 这个也是本机连接提供者的配置
     */
    Class<? extends ProxyConfig> proxy() default NotConfig.class;

    /**
     * 配置这个服务的相关信息
     */
    Class<? extends ServiceConfig> config() default NotConfig.class;

}
