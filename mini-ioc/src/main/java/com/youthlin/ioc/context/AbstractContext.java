package com.youthlin.ioc.context;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.IAnnotationProcessor;
import com.youthlin.ioc.annotaion.SimpleAnnotationProcessor;
import com.youthlin.ioc.exception.BeanDefinitionException;
import com.youthlin.ioc.exception.NoSuchBeanException;
import com.youthlin.ioc.spi.IPreScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    protected Set<String> unloadedClassName = Collections.synchronizedSet(new HashSet<String>());
    protected IAnnotationProcessor processor = new SimpleAnnotationProcessor();

    public AbstractContext() {
        this("");
    }

    public AbstractContext(String... scanPackages) {
        this(null, scanPackages);
    }

    public AbstractContext(List<IPreScanner> preScannerList) {
        this(preScannerList, "");
    }

    public AbstractContext(List<IPreScanner> preScannerList, String... scanPackages) {
        if (preScannerList != null) {
            for (IPreScanner preScanner : preScannerList) {
                try {
                    preScanner.preScan(this);
                } catch (Exception e) {
                    LOGGER.error("PreScanner Error {}", preScanner, e);//不能影响主流程
                }
            }
        }
        processor.autoScan(this, scanPackages);
    }

    @Override
    public void registerBean(Object bean) {
        registerBean(bean, bean.getClass().getSimpleName());
    }

    @Override
    public void registerBean(Object bean, String name) {
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

    @Override
    public Object getBean(String name) {
        return getNameBeanMap().get(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Class<T> clazz) {
        try {
            return AnnotationUtil.getBean(getClazzBeanMap(), clazz);
        } catch (NoSuchBeanException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        Object o = getNameBeanMap().get(name);
        return (T) o;
    }

    @Override
    public <T> Set<T> getBeans(Class<T> clazz) {
        Set<T> set = new HashSet<>();
        set.addAll(AnnotationUtil.getBeans(getClazzBeanMap(), clazz));
        return set;
    }

    @Override
    public Object[] getBeans(Class<?>[] classes) {
        Object[] beans = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            beans[i] = getBean(classes[i]);
        }
        return beans;
    }

    @Override
    public Set<Object> getBeans() {
        Set<Object> set = new HashSet<>();
        for (Map.Entry<Class, Object> entry : getClazzBeanMap().entrySet()) {
            set.add(entry.getValue());
        }
        return set;
    }

    @Override
    public int getBeanCount() {
        return getClazzBeanMap().size();
    }

    @Override
    public Map<String, Object> getNameBeanMap() {
        return Collections.unmodifiableMap(nameBeanMap);
    }

    @Override
    public Map<Class, Object> getClazzBeanMap() {
        return Collections.unmodifiableMap(clazzBeanMap);
    }

    @Override
    public Set<String> getUnloadedClass() {
        return Collections.unmodifiableSet(unloadedClassName);
    }

    @Override
    public boolean addUnloadedClass(String className) {
        return unloadedClassName.add(className);
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
