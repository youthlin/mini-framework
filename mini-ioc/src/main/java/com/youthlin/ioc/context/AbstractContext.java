package com.youthlin.ioc.context;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.IAnnotationProcessor;
import com.youthlin.ioc.annotaion.SimpleAnnotationProcessor;
import com.youthlin.ioc.exception.BeanDefinitionException;
import com.youthlin.ioc.exception.NoSuchBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 13:50.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractContext implements Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContext.class);
    protected Map<String, Object> nameBeanMap = new ConcurrentHashMap<>();
    protected Map<Class, Object> clazzBeanMap = new ConcurrentHashMap<>();
    protected Set<String> unloadedClassName = new HashSet<>();
    protected IAnnotationProcessor processor = new SimpleAnnotationProcessor();

    public AbstractContext() {
        this("");
    }

    public AbstractContext(String... scanPackages) {
        processor.autoScan(this, scanPackages);
        LOGGER.debug("name  map:{}", getNameBeanMap());
        LOGGER.debug("class map:{}", getClazzBeanMap());
    }

    @Override public void registerBean(Object bean) {
        String name = bean.getClass().getSimpleName();
        registerBean(bean, name);
    }

    @Override public void registerBean(Object bean, String name) {
        Class<?> beanClass = bean.getClass();
        Object existBean = nameBeanMap.get(name);
        if (existBean != null) {
            //重名
            throw new BeanDefinitionException(
                    "There is already a bean named [" + name + "] found when register bean: " + bean);
        }
        existBean = clazzBeanMap.get(beanClass);
        if (existBean != null) {
            throw new BeanDefinitionException("There is already a bean of class: " + beanClass.getName());
        }
        nameBeanMap.put(name, bean);
        clazzBeanMap.put(beanClass, bean);
    }

    @Override public Object getBean(String name) {
        return getNameBeanMap().get(name);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getBean(Class<T> clazz) {
        try {
            return AnnotationUtil.getBean(getClazzBeanMap(), clazz);
        } catch (NoSuchBeanException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getBean(String name, Class<T> clazz) {
        Object o = getNameBeanMap().get(name);
        return (T) o;
    }

    @Override public <T> Set<T> getBeans(Class<T> clazz) {
        Set<T> set = new HashSet<>();
        set.addAll(AnnotationUtil.getBeans(getClazzBeanMap(), clazz));
        return set;
    }

    @Override public int getBeanCount() {
        return getClazzBeanMap().size();
    }

    @Override public Set<Object> getBeans() {
        Set<Object> set = new HashSet<>();
        for (Map.Entry<Class, Object> entry : getClazzBeanMap().entrySet()) {
            set.add(entry.getValue());
        }
        return set;
    }

    @Override public Map<String, Object> getNameBeanMap() {
        return nameBeanMap;
    }

    @Override public Map<Class, Object> getClazzBeanMap() {
        return clazzBeanMap;
    }

    @Override public Set<String> getUnloadedClass() {
        return unloadedClassName;
    }

    protected AbstractContext setNameBeanMap(Map<String, Object> nameBeanMap) {
        this.nameBeanMap = nameBeanMap;
        return this;
    }

    protected AbstractContext setClazzBeanMap(Map<Class, Object> clazzBeanMap) {
        this.clazzBeanMap = clazzBeanMap;
        return this;
    }

    protected AbstractContext setUnloadedClass(Set<String> unloadedClass) {
        this.unloadedClassName = unloadedClass;
        return this;
    }
}
