package com.youthlin.ioc.annotation;

import com.youthlin.ioc.context.BeanPostProcessor;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.exception.BeanDefinitionException;
import com.youthlin.ioc.exception.BeanInjectException;
import com.youthlin.ioc.exception.NoSuchBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 13:33.
 */
@SuppressWarnings("WeakerAccess")
public class SimpleAnnotationProcessor implements IAnnotationProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAnnotationProcessor.class);

    /**
     * 对包路径进行自动扫描
     */
    @Override
    public void autoScan(Context context, String... scanPackages) {
        Set<String> classNames = new HashSet<>(AnnotationUtil.getClassNames(scanPackages));
        LOGGER.trace("class names in scan package: {}", classNames);
        // 构造 Bean
        for (String className : classNames) {
            registerClass(context, className);
        }
        afterRegister(context);
        // 注入需要的字段
        for (Map.Entry<Class, Object> entry : context.getClazzBeanMap().entrySet()) {
            Object obj = entry.getValue();
            injectFiled(context, obj);
        }
        afterInjected(context);
        // PostConstruct
        for (Object bean : context.getBeans()) {
            postConstruct(context, bean);
        }
        done(context);
    }

    /**
     * 扫描类，当类有注解时，注册到容器中
     *
     * @param className 类的全限定名
     * @throws BeanDefinitionException 1. 找不到类时;
     *                                 2. 加载类后, 并且类中有注解, 但:
     *                                 类不能访问时(如构造器不是 public),
     *                                 该类是接口或抽象类而不能实例化时时;
     *                                 3. 有重复名称的 Bean 存在时;
     *                                 4. 有重复类型的 Bean 存在时
     */
    protected void registerClass(Context context, String className) {
        try {
            //会触发类的 static 块，但不会触发构造函数和实例初始化块
            Class<?> aClass = Class.forName(className);
            if (aClass.isAnnotation()) {
                LOGGER.trace("skip register annotation {}", aClass);
                return;
            }
            Resource annotation = AnnotationUtil.getAnnotation(aClass, Resource.class);
            if (annotation != null) {
                String name = AnnotationUtil.getBeanName(aClass);
                if (shouldNewInstance(aClass)) {
                    Object o = aClass.newInstance();
                    for (BeanPostProcessor beanPostProcessor : context.getBeans(BeanPostProcessor.class)) {
                        o = beanPostProcessor.postProcess(o, aClass, name);
                    }
                    context.registerBean(o, name);
                    LOGGER.debug("find bean: {}, name: {}, annotations: {}", o.getClass(), name,
                            Arrays.toString(aClass.getAnnotations()));
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError | UnsatisfiedLinkError e) {
            context.addUnloadedClass(className);
            LOGGER.trace("can not load class: {}", className, e);//很常见的 第三方依赖的类可能不在 classpath 中
        } catch (Throwable e) {
            context.addUnloadedClass(className);
            LOGGER.warn("can not load class: {}", className, e);
        }
    }

    protected boolean shouldNewInstance(Class c) {
        return AnnotationUtil.shouldNewInstance(c);
    }

    protected void afterRegister(Context context) {
        LOGGER.debug("register beans: {}", context.getBeanCount());
    }

    /**
     * 注入该 Bean 中需要的字段.
     * 字段上定义了名称时，只按名称查找；没定义名称时，按类型查找
     * 支持注入同一个类型的 Bean 到集合或 Map 中, 即 可以这样写:
     * <pre>
     *     {@code @Bean} private Map&lt;String, IUserDao&gt; userDaoMap;
     * </pre>
     * 但仅支持一层泛型, 所以不能这样用: {@code @Bean private List<List<IUserDao>> userDaoList;}
     * <p>
     * 如果字段是集合类，且已经初始化，那么支持 Collection, Map 及其子类
     * 如果字段是集合类，但没有舒适化，那么仅支持 Collection, List, Set, Map
     *
     * @throws NoSuchBeanException 当找不到需要注入的 Bean 时
     * @throws BeanInjectException 当字段已有值，且不是 Collection, Map 时;
     *                             当不能注入 Bean 到字段中时
     */
    @SuppressWarnings("unchecked")
    protected void injectFiled(Context context, Object object) {
        Class<?> objClass = object.getClass();
        Field[] fields = objClass.getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                int fieldModifiers = field.getModifiers();
                if (Modifier.isStatic(fieldModifiers) || Modifier.isFinal(fieldModifiers)) {
                    continue;//静态的, Final 的不需要处理
                }
                Resource resource = AnnotationUtil.getAnnotation(field, Resource.class);
                if (resource != null) {
                    //Resource 注入规则: 优先按照名字注入, 若没有再按照类型注入, 若指定了 @Resource(name='xxx') 则只按名字注入
                    Object filedValue;
                    String name = resource.name();
                    if (!name.isEmpty()) {//如果注解指定了名称，只按名称查找 Bean
                        filedValue = context.getNameBeanMap().get(name);
                    } else {
                        //优先按照 FieldName 查找
                        name = field.getName();
                        //name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        filedValue = context.getNameBeanMap().get(name);
                        if (filedValue == null) {
                            //找不到按类型查找
                            filedValue = getFiledValueByType(context, field, object);
                        }
                    }
                    if (filedValue == null) {
                        throw new NoSuchBeanException(name);
                    }
                    if (!field.getType().isAssignableFrom(filedValue.getClass())) {
                        //按名称注入可能类型不兼容
                        throw new NoSuchBeanException("Bean named " + name + " is expected to be of type \'" + field.getType() + "\' but was actually of type \'" + filedValue.getClass() + '\'');
                    }
                    if (!AnnotationUtil.setFiledValue(object, field, filedValue)) {
                        throw new BeanInjectException("Can not inject field " + field + " to class " + objClass);
                    }
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Object getFiledValueByType(Context context, Field field, Object object) {
        Class<?> type = field.getType();
        Object filedValue = null;
        try {
            field.setAccessible(true);
            filedValue = field.get(object);
        } catch (IllegalAccessException ignore) {
        }
        if (filedValue != null) {
            //集合类可能已经初始化 @Resource private Map<String, IUserDao> userDaoMap = new HashMap<>();
            if (Collection.class.isAssignableFrom(type)) {//这个字段可以强制转换为 Collection
                ((Collection) filedValue).addAll(AnnotationUtil.getBeans(context.getClazzBeanMap(),
                        AnnotationUtil.getGenericClass(field, 0)));
            } else if (Map.class.isAssignableFrom(type)) {//这个字段可以强制转换为 Map
                ((Map) filedValue).putAll(AnnotationUtil
                        .getBeansMap(context.getClazzBeanMap(), AnnotationUtil.getGenericClass(field, 1)));
            } else {
                LOGGER.warn("{}, {}", field, filedValue);
                throw new BeanInjectException("this field already has a value but also has @Bean.");
            }
        } else {
            if (type.isAssignableFrom(Collection.class) || type.isAssignableFrom(List.class)) {
                filedValue = AnnotationUtil
                        .getBeans(context.getClazzBeanMap(), AnnotationUtil.getGenericClass(field, 0));
            } else if (type.isAssignableFrom(Set.class)) {
                filedValue = new HashSet(AnnotationUtil.getBeans(
                        context.getClazzBeanMap(), AnnotationUtil.getGenericClass(field, 0)));
            } else if (type.isAssignableFrom(Map.class)) {
                filedValue = AnnotationUtil
                        .getBeansMap(context.getClazzBeanMap(), AnnotationUtil.getGenericClass(field, 1));
            } else {
                filedValue = AnnotationUtil.getBean(context.getClazzBeanMap(), type);
            }
        }
        return filedValue;
    }

    protected void afterInjected(Context context) {
        LOGGER.debug("{} Beans inited.", context.getBeanCount());
    }

    protected void postConstruct(Context context, Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        if (methods != null) {
            for (Method method : methods) {
                PostConstruct postConstruct = AnnotationUtil.getAnnotation(method, PostConstruct.class);
                if (postConstruct != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] parameters = context.getBeans(parameterTypes);
                    try {
                        method.setAccessible(true);
                        method.invoke(bean, parameters);
                    } catch (ReflectiveOperationException e) {
                        LOGGER.error("Error occurs when invoke PostConstruct method {} of bean {}", method, bean, e);
                    }
                    break;//bean should not have more than one PostConstruct
                }
            }
        }
    }

    protected void done(Context context) {
        LOGGER.debug("scan done. unloaded class: {}", context.getUnloadedClass().size());
    }
}
