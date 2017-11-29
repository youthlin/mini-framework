package com.youthlin.ioc.annotation;

import com.youthlin.ioc.exception.NoSuchBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 14:48.
 */
@SuppressWarnings("WeakerAccess")
public class AnnotationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationUtil.class);
    private static final String UTF_8 = "UTF-8";
    private static final String FORWARD_CHAR = "/";
    private static final String META_INF = "META-INF";
    private static final String DOT_CLASS = ".class";
    private static final String DOLLAR = "$";
    private static final String DOT = ".";
    private static final String VALUE = "value";
    private static final FileFilter FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }
            String name = pathname.getName();
            return !name.contains(DOLLAR) && name.endsWith(DOT_CLASS);
        }
    };

    /**
     * 获取包路径之下的所有类名 不包括内部类和非 .class 结尾的类
     */
    public static Set<String> getClassNames(String... basePackages) {
        Set<String> classNames = new HashSet<>();
        if (basePackages != null) {
            for (String basePackage : basePackages) {
                classNames.addAll(getClassNames(basePackage));
            }
        }
        return classNames;
    }

    private static List<String> getClassNames(String basePackage) {
        List<String> classNames = new ArrayList<>();
        try {
            Enumeration<URL> systemResources = Thread.currentThread().getContextClassLoader()
                    .getResources(basePackage.replace(DOT, FORWARD_CHAR));
            while (systemResources.hasMoreElements()) {
                URL url = systemResources.nextElement();
                if (url != null) {
                    classNames.addAll(getClassNamesFromUrl(basePackage, url));
                }
            }
        } catch (IOException ignore) {
        }
        if (basePackage.isEmpty()) {
            classNames.addAll(getClassNames(META_INF));
        }
        return classNames;
    }

    private static List<String> getClassNamesFromUrl(String basePackage, URL url) {
        List<String> classNames = new ArrayList<>();
        LOGGER.debug("scan url = {}", url);
        String protocol = url.getProtocol();
        switch (protocol) {
            case "file":
                classNames.addAll(getClassNamesFromFileSystem(basePackage, url));
                break;
            case "jar":
                classNames.addAll(getClassNamesFromJar(basePackage, url));
                break;
            default:
                LOGGER.warn("unknown protocol. [{}]", protocol);
        }
        return classNames;
    }

    private static List<String> getClassNamesFromFileSystem(String basePackage, URL url) {
        List<String> classNames = new ArrayList<>();
        File file;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        File[] files = file.listFiles(FILE_FILTER);
        if (files != null) {
            for (File file1 : files) {
                classNames.addAll(getClassNamesFromFileSystem(basePackage, file1));
            }
        }

        return classNames;
    }

    private static List<String> getClassNamesFromFileSystem(String packageName, File file) {
        List<String> classNames = new ArrayList<>();
        String fileName = file.getName();
        if (file.isDirectory()) {
            File[] files = file.listFiles(FILE_FILTER);
            if (files == null) {
                return Collections.emptyList();
            }
            for (File classFile : files) {
                if (packageName.length() == 0) {
                    classNames.addAll(getClassNamesFromFileSystem(fileName, classFile));
                } else {
                    classNames.addAll(getClassNamesFromFileSystem(packageName + DOT + fileName, classFile));
                }
            }
        } else {
            LOGGER.trace("package:{} class:{}", packageName, fileName);
            classNames.add(packageName + DOT + fileName.substring(0, fileName.lastIndexOf(DOT_CLASS)));
        }
        return classNames;
    }

    private static List<String> getClassNamesFromJar(String basePackage, URL url) {
        List<String> classNames = new ArrayList<>();
        if (META_INF.equals(basePackage)) {
            basePackage = "";
        }
        String jarFileName = url.toString();
        try {
            jarFileName = URLDecoder.decode(jarFileName, UTF_8);
        } catch (UnsupportedEncodingException ignore) {
        }
        jarFileName = jarFileName.replace("%20", " ");
        jarFileName = jarFileName.substring("jar:file:".length());
        int indexOf = jarFileName.indexOf("!/");
        if (indexOf > 0) {
            jarFileName = jarFileName.substring(0, indexOf);
        }
        try {
            JarFile jarFile = new JarFile(jarFileName);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                name = name.replace("/", DOT);//jar 内都是 /
                if (name.endsWith(DOT_CLASS) && !name.contains(DOLLAR) && name.startsWith(basePackage)) {
                    name = name.substring(0, name.lastIndexOf(DOT_CLASS));
                    classNames.add(name);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classNames;
    }

    /**
     * 获取字段上注解的名称
     *
     * @return 如果注解定义了名称，直接返回；否则返回空字符串
     */
    static String getAnnotationName(Field field) {
        Resource resource = getAnnotation(field, Resource.class);
        String name = "";
        if (resource != null) {
            name = resource.name();
        }
        return name;
    }

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> clazz) {
        try {
            if (shouldNewInstance(clazz)) {
                return (T) clazz.newInstance();
            }
        } catch (ReflectiveOperationException ignore) {
        }
        return null;
    }

    public static boolean shouldNewInstance(Class c) {
        int modifiers = c.getModifiers();
        return !Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers);
    }

    /**
     * Get a single {@link Annotation} of {@code annotationType} from the supplied
     * Method, Constructor or Field. Meta-annotations will be searched if the annotation
     * is not declared locally on the supplied element.
     *
     * @param ae             the Method, Constructor or Field from which to get the annotation
     * @param annotationType the annotation class to look for, both locally and as a meta-annotation
     * @return the matching annotation or {@code null} if not found
     * @since 3.1
     */
    public static <T extends Annotation> T getAnnotation(AnnotatedElement ae, Class<T> annotationType) {
        T ann = ae.getAnnotation(annotationType);
        if (ann == null) {
            for (Annotation metaAnn : ae.getAnnotations()) {
                ann = metaAnn.annotationType().getAnnotation(annotationType);
                if (ann != null) {
                    break;
                }
            }
        }
        return ann;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValueOf(Class<T> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(int.class)) {
                return (T) Integer.valueOf(0);
            } else if (clazz.equals(float.class)) {
                return (T) Float.valueOf(0);
            } else if (clazz.equals(double.class)) {
                return (T) Double.valueOf(0);
            } else if (clazz.equals(long.class)) {
                return (T) Long.valueOf(0);
            } else if (clazz.equals(char.class)) {
                return (T) Character.valueOf((char) 0);
            } else if (clazz.equals(byte.class)) {
                return (T) Byte.valueOf((byte) 0);
            } else if (clazz.equals(short.class)) {
                return (T) Short.valueOf((short) 0);
            } else if (clazz.equals(boolean.class)) {
                return (T) Boolean.FALSE;
            }//void
        }
        return null;
    }

    /**
     * Retrieve the <em>value</em> of the {@code &quot;value&quot;} attribute of a
     * single-element Annotation, given an annotation instance.
     *
     * @param annotation the annotation instance from which to retrieve the value
     * @return the attribute value, or {@code null} if not found
     * @see #getValue(Annotation, String)
     */
    public static Object getValue(Annotation annotation) {
        return getValue(annotation, VALUE);
    }

    /**
     * Retrieve the <em>value</em> of a named Annotation attribute, given an annotation instance.
     *
     * @param annotation    the annotation instance from which to retrieve the value
     * @param attributeName the name of the attribute value to retrieve
     * @return the attribute value, or {@code null} if not found
     * @see #getValue(Annotation)
     */
    public static Object getValue(Annotation annotation, String attributeName) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attributeName);
            return method.invoke(annotation);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Retrieve the <em>default value</em> of the {@code &quot;value&quot;} attribute
     * of a single-element Annotation, given an annotation instance.
     *
     * @param annotation the annotation instance from which to retrieve the default value
     * @return the default value, or {@code null} if not found
     * @see #getDefaultValue(Annotation, String)
     */
    public static Object getDefaultValue(Annotation annotation) {
        return getDefaultValue(annotation, VALUE);
    }

    /**
     * Retrieve the <em>default value</em> of a named Annotation attribute, given an annotation instance.
     *
     * @param annotation    the annotation instance from which to retrieve the default value
     * @param attributeName the name of the attribute value to retrieve
     * @return the default value of the named attribute, or {@code null} if not found
     * @see #getDefaultValue(Class, String)
     */
    public static Object getDefaultValue(Annotation annotation, String attributeName) {
        return getDefaultValue(annotation.annotationType(), attributeName);
    }

    /**
     * Retrieve the <em>default value</em> of the {@code &quot;value&quot;} attribute
     * of a single-element Annotation, given the {@link Class annotation type}.
     *
     * @param annotationType the <em>annotation type</em> for which the default value should be retrieved
     * @return the default value, or {@code null} if not found
     * @see #getDefaultValue(Class, String)
     */
    public static Object getDefaultValue(Class<? extends Annotation> annotationType) {
        return getDefaultValue(annotationType, VALUE);
    }

    /**
     * Retrieve the <em>default value</em> of a named Annotation attribute, given the {@link Class annotation type}.
     *
     * @param annotationType the <em>annotation type</em> for which the default value should be retrieved
     * @param attributeName  the name of the attribute value to retrieve.
     * @return the default value of the named attribute, or {@code null} if not found
     * @see #getDefaultValue(Annotation, String)
     */
    public static Object getDefaultValue(Class<? extends Annotation> annotationType, String attributeName) {
        try {
            Method method = annotationType.getDeclaredMethod(attributeName);
            return method.getDefaultValue();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获取注解(/Controller/Service/Dao/Resource)中定义的名称.
     *
     * @return 如果注解定义了名称，返回名称，否则返回类名
     * @throws IllegalArgumentException 当类没有被注解时
     */
    static String getAnnotationName(Class<?> clazz) {
        Resource resourceAnnotation = AnnotationUtil.getAnnotation(clazz, Resource.class);
        if (resourceAnnotation == null) {
            throw new IllegalArgumentException("No @Resource annotation at this object.");
        }
        String name = (String) getValue(clazz, resourceAnnotation, "name");
        if (name == null || name.isEmpty()) {
            name = clazz.getSimpleName();
        }
        return name;
    }

    public static Object getValue(AnnotatedElement ae, Annotation annotation) {
        return getValue(ae, annotation, VALUE);
    }

    public static Object getValue(AnnotatedElement ae, Annotation annotation, String attributeName) {
        Annotation anno = ae.getAnnotation(annotation.annotationType());
        if (anno != null) {//anno 直接注解在 clazz 上
            return getValue(anno, attributeName);
        }
        for (Annotation at : ae.getAnnotations()) {
            Annotation ata = at.annotationType().getAnnotation(annotation.annotationType());
            if (ata != null) {//ata 注解在 at 上, at 注解在 clazz 上
                return getValue(at, attributeName);
            }
        }
        return null;
    }

    public static boolean setFiledValue(Object targetObject, Field field, Object value) {
        String fieldName = field.getName();
        Class<?> classType = targetObject.getClass();
        Class<?> fieldType = field.getType();
        Method setMethod = null;
        try {
            String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            setMethod = classType.getMethod(setMethodName, fieldType);
            setMethod.invoke(targetObject, value);
        } catch (NoSuchMethodException ignore) {
            try {//没有 set 方法 直接设置
                field.setAccessible(true);
                field.set(targetObject, value);
            } catch (IllegalAccessException e) {
                LOGGER.warn("Can not set filed {} of {} from {}", fieldName, classType, value, e);
                return false;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Invoke setter method error: {} value: {}", setMethod, value, e);
            return false;
        }
        return true;
    }

    /**
     * 获取字段的泛型类型.
     *
     * @param field 要处理的字段
     * @param index 泛型列表中第几个, 0开始. 如{@code Map<String, Object> map} 0 表示第一个 String.class, 1 表示第二个 Object.class
     * @return 泛型的类型
     * @throws IllegalArgumentException  当字段不是泛型时 或超过一层泛型时
     * @throws IndexOutOfBoundsException 当下标越界时
     */
    public static Class getGenericClass(Field field, int index) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type type = ((ParameterizedType) genericType).getActualTypeArguments()[index];
            if (Class.class.isAssignableFrom(type.getClass())) {
                return (Class) type;
            }
            throw new IllegalArgumentException("Field has more than one level of generic");
        }
        throw new IllegalArgumentException("field is not generic: " + field);
    }

    public static <T> T getBean(Map<Class, Object> clazzBeanMap, Class<T> clazz) {
        List<T> list = getBeans(clazzBeanMap, clazz);
        if (list.isEmpty()) {
            throw new NoSuchBeanException(clazz.getName());
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new NoSuchBeanException("find more than one bean with type: " + clazz.getName());

    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeans(Map<Class, Object> clazzBeanMap, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        T o = (T) clazzBeanMap.get(clazz);
        if (o != null) {
            list.add(o);
        } else {
            for (Map.Entry<Class, Object> entry : clazzBeanMap.entrySet()) {
                Class aClass = entry.getKey();
                if (clazz.isAssignableFrom(aClass)) {
                    //该类可以赋值给 clazz (即，是 clazz 的子类)
                    list.add((T) entry.getValue());
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getBeansMap(Map<Class, Object> clazzBeanMap, Class<T> clazz) {
        Map<String, T> map = new HashMap<>();
        T o = (T) clazzBeanMap.get(clazz);
        if (o != null) {
            String name = AnnotationUtil.getAnnotationName(o.getClass());
            map.put(name, o);
        } else {
            for (Map.Entry<Class, Object> entry : clazzBeanMap.entrySet()) {
                Class aClass = entry.getKey();
                if (clazz.isAssignableFrom(aClass)) {
                    //aClass 类可以赋值给 clazz (aClass 是 clazz 的子类)
                    String name = AnnotationUtil.getAnnotationName(aClass);
                    map.put(name, (T) entry.getValue());
                }
            }
        }
        return map;
    }

}
