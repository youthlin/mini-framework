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
 * 使用 JUnit 测试时, 可以使用 {@link org.junit.runner.RunWith} 注解指定 {@link org.junit.runner.Runner}
 * <p>
 * <pre>
 *     &#064;RunWith(MiniRunner.class)
 *     &#064;Scan("com.example")
 *     &#064;Resource
 *     public class ServiceTest{
 *         &#064;Resource
 *         private UserDao userDao;
 *         public void save(){
 *             userDao.save(...);
 *         }
 *     }
 * </pre>
 * 创建: youthlin.chen
 * 时间: 2017-12-05 23:25
 *
 * @see Scan
 */
public class MiniRunner extends BlockJUnit4ClassRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiniRunner.class);
    private Context context;

    /**
     * {@inheritDoc}
     * 使用 MiniRunner 会构建一个 {@link Context}, 因此 测试类 如果是容器托管的, 那么也可以注入容器内其他的 Bean
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
            scanPackages = new String[]{packageName};
        }
        context = new ClasspathContext(scanPackages);
    }

    /**
     * {@inheritDoc}
     * 默认从容器中取出测试类实例, 若容器内没有且该类依赖了其他 Bean 则抛出异常.
     * 否则调用 JUnit 父类逻辑使用默认构造方法生成测试类的实例
     *
     * @throws IllegalAccessException 当测试类没有 &#064;Resource 注解 但其有字段含有 &#064;Resource 注解（即只有容器托管的才能注入）
     */
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
                throw new IllegalAccessException("Only IoC managed bean can inject field.");
            }
        }
        return super.createTest();
    }
}
