package com.youthlin.rpc.annotation;

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
     * 配置这个服务的相关信息
     */
    Class<? extends ServiceConfig> config() default ServiceConfig.class;

}
