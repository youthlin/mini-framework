package com.youthlin.aop.test.aop;

import com.youthlin.aop.annotation.Aop;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2018-01-28 19:42
 */
@Aop
@Aspect
public class AopService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AopService.class);

    @Around("execution(* com.youthlin.aop.test.service.IHelloService.sayHello(**))")
    public Object around(ProceedingJoinPoint pjp) {
        try {
            Object proxy = pjp.getThis();
            Object target = pjp.getTarget();
            Object[] args = pjp.getArgs();
            LOGGER.info("proxy={}, target={}, args={}", proxy, target, args);
            Object proceed = pjp.proceed(args);
            LOGGER.info("proceed={}", proceed);
            return proceed;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    static public void main(String[] args) {
        Class<AopService> clazz = AopService.class;
        LOGGER.info("{}", clazz.getName());
        for (Method method : clazz.getDeclaredMethods()) {
            String s = method.toString();
            LOGGER.info("{}", s);
            LOGGER.info("{}", s.matches(".*\\.around\\(.*\\)"));
        }
    }

}
