package com.youthlin.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * 环绕通知 作用于 AOP 类的方法上
 * <p>
 * 创建: youthlin.chen
 * 时间: 2018-01-29 19:35
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {
    /**
     * Method 示例 toString 后, 把点号替换为斜线 类名和方法间的点号替换为井号.用处理后的这个字符串来 match 这里的 value.
     * <ul>
     * <li><pre> public static void com.youthlin.aop.test.aop.AopService.main(java.lang.String[]) </pre></li>
     * <li><pre> .*\.methodName\(.*\) </pre></li>
     * </ul>
     *
     * @see Method#toString()
     * @see String#matches(String)
     */
    String value();

}
