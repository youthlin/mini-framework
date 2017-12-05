package com.youthlin.ioc.test;

import com.youthlin.ioc.annotation.AnnotationUtil;
import com.youthlin.ioc.context.ClasspathContext;
import com.youthlin.ioc.context.Context;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * 创建: youthlin.chen
 * 时间: 2017-12-05 23:25
 */
public class MiniRunner extends BlockJUnit4ClassRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiniRunner.class);
    private Context context;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public MiniRunner(Class<?> klass) throws InitializationError {
        super(klass);
        buildContext(klass);
    }

    private void buildContext(Class<?> clazz) {
        String[] scanPackages;
        Scan scan = AnnotationUtil.getAnnotation(clazz, Scan.class);
        if (scan != null) {
            scanPackages = scan.value();
        } else {
            String packageName = "";
            String className = clazz.getName();
            int i = className.lastIndexOf(".");
            if (i > 0) {
                packageName = className.substring(0, i);
            }
            scanPackages = new String[] { packageName };
        }
        context = new ClasspathContext(scanPackages);
    }

    @Override
    protected Object createTest() throws Exception {
        Class<?> javaClass = getTestClass().getJavaClass();
        Object bean = context.getBean(javaClass);
        if (bean != null) {
            return bean;
        } else {
            boolean fail = false;
            for (Field field : javaClass.getDeclaredFields()) {
                if (AnnotationUtil.getAnnotation(field, Resource.class) != null) {
                    LOGGER.warn(
                            "Test Instance {} has @Resource annotation filed {}, but itself is not managed by IoC container.",
                            javaClass.getSimpleName(), field.getName());
                    fail = true;
                }
            }
            if (fail) {
                throw new IllegalAccessException("Only IoC management bean can inject filed.");
            }
        }
        return super.createTest();
    }
}
