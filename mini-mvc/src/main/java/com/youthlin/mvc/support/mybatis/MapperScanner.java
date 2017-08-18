package com.youthlin.mvc.support.mybatis;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.Dao;
import com.youthlin.ioc.context.Context;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-18 18:07.
 */
@Resource
public class MapperScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperScanner.class);
    private String annotationClassName = Dao.class.getName();
    private String configFile = "mybatis/config.xml";
    private String[] basePackages = { "" };
    private Context context;
    private Map<Class, Object> mappers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void scan(Context context) {
        this.context = context;
        Class<? extends Annotation> scanAnnotation;
        try {
            scanAnnotation = (Class<? extends Annotation>) Class.forName(annotationClassName);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
        if (!scanAnnotation.isAnnotation()) {
            throw new IllegalArgumentException("scan annotation class name error.");
        }
        InputStream in;
        try {
            in = Resources.getResourceAsStream(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        SqlSessionManager manager = SqlSessionManager.newInstance(factory);//MyBatis 自带的 线程安全的 SqlSession
        Set<String> classNames = AnnotationUtil.getClassNames(basePackages);
        for (String className : classNames) {
            try {
                Class<?> aClass = Class.forName(className);
                Annotation annotation = AnnotationUtil.getAnnotation(aClass, scanAnnotation);
                if (annotation != null) {
                    Object mapper = manager.getMapper(aClass);
                    registerMapper(mapper);
                }
            } catch (Throwable e) {
                LOGGER.debug("", e);
            }
        }
    }

    private void registerMapper(Object mapper) {
        context.registerBean(mapper);
        mappers.put(mapper.getClass(), mapper);
    }

}
