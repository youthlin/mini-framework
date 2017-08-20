package com.youthlin.ioc.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当用在类上时，表明该类是一个"服务提供者",可注入到其他类中,
 * 当用在字段属性或方法上时，表明该字段属性或方法需要注入.
 * <pre>
 *     {@code @Bean}
 *     public class Dao{
 *
 *     }
 *     {@code @Bean}
 *     public class Service{
 *         {@code @Bean}
 *         private Dao dao;
 *     }
 * </pre>
 * 可以指定 value 作为 bean 的名称。
 * 当注入时，若有名称，则优先使用名称查找，找不到则抛出异常；
 * 若注入时没有指定名称，则根据类型查找，若有多个也会抛出异常
 * 创建： youthlin.chen
 * 时间： 2017-08-10 19:29.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    String value() default "";
}
