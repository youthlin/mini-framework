package com.youthlin.ioc.context;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.IAnnotationProcessor;
import com.youthlin.ioc.annotaion.SimpleAnnotationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 13:50.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractContext implements Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContext.class);
    protected IAnnotationProcessor processor = new SimpleAnnotationProcessor();

    public AbstractContext() {
        this("");
    }

    public AbstractContext(String... scanPackages) {
        processor.autoScan(scanPackages);
        LOGGER.info("name  map:{}", processor.getNameBeanMap());
        LOGGER.info("class map:{}", processor.getClazzBeanMap());
    }

    @Override public Object getBean(String name) {
        return processor.getNameBeanMap().get(name);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getBean(Class<T> clazz) {
        return AnnotationUtil.getBean(processor.getClazzBeanMap(), clazz);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getBean(String name, Class<T> clazz) {
        Object o = processor.getNameBeanMap().get(name);
        return (T) o;
    }

    @Override public int getBeanCount() {
        return processor.getClazzBeanMap().size();
    }

    @Override public Set<Object> getBeans() {
        Set<Object> set = new HashSet<>();
        for (Map.Entry<Class, Object> entry : processor.getClazzBeanMap().entrySet()) {
            set.add(entry.getValue());
        }
        return set;
    }

    @Override public Set<String> getUnloadedClass() {
        return processor.getUnloadedClass();
    }

}
