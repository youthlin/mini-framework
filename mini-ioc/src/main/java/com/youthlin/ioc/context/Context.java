package com.youthlin.ioc.context;

import java.util.Map;
import java.util.Set;

/**
 * 容器上下文.
 * 创建： youthlin.chen
 * 时间： 2017-08-10 13:31.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface Context {
    /**
     * 往容器中注册一个 Bean, 注册名称为类名
     */
    void registerBean(Object bean);

    /**
     * 往容器中注册一个 Bean, 指定 Bean 的名称
     */
    void registerBean(Object bean, String name);

    /**
     * 根据名称获取 Bean, 当找不到时返回{@code null}
     */
    Object getBean(String name);

    /**
     * 根据类型获取 Bean, 当找不到时返回{@code null}
     */
    <T> T getBean(Class<T> clazz);

    /**
     * 根据名称和类型获取 Bean, 当找不到或找到多个时返回{@code null}
     */
    <T> T getBean(String name, Class<T> clazz);

    /**
     * 根据类型获取 Bean
     */
    <T> Set<T> getBeans(Class<T> clazz);

    /**
     * 根据类型获取 Bean 数组，结果数组和传入的参数类型一一对应
     */
    Object[] getBeans(Class<?>[] classes);

    /**
     * 获取容器中 Bean 的总数
     */
    int getBeanCount();

    /**
     * 获取名称与 Bean 的对应关系
     */
    Map<String, Object> getNameBeanMap();

    /**
     * 获取类型与 Bean 的对应关系
     */
    Map<Class, Object> getClazzBeanMap();

    /**
     * 获取所有的 Bean
     */
    Set<Object> getBeans();

    /**
     * 获取未加载的类名.
     * 有些类在扫描路径中，但可能加载失败，
     * 如依赖一个第三方 jar 中类 A,
     * 依赖另一个不在 classpath 的 jar 包，那么类 A 将会加载失败
     */
    Set<String> getUnloadedClass();

    boolean addUnloadedClass(String className);
}
