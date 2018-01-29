package com.youthlin.aop.annotation;

import javax.annotation.Resource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个类是一个 Aop 类型的 Service. 在 @Aop 的 Service 中可以统一定义拦截的方法和行为.
 * <p>
 * 创建: youthlin.chen
 * 时间: 2018-01-28 19:49
 */
@Resource
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Aop {

}
