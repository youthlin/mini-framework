package com.youthlin.ioc.context;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-26 13:10
 */
public interface BeanPostProcessor {
    /**
     * 实例化一个 bean 后, 放入容器之前调用
     *
     * @param bean     创建的 bean 实例
     * @param beanName bean 名字
     * @return 要注册到容器的实例
     */
    Object postProcess(Object bean, String beanName);
}
