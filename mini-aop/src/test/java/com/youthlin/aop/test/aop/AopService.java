package com.youthlin.aop.test.aop;

import com.youthlin.aop.core.ProceededJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * 创建: youthlin.chen
 * 时间: 2018-01-28 19:42
 */
@Resource
@Aspect
public class AopService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AopService.class);

    @Pointcut("execution(* com.youthlin.aop.test.service.IHelloService.sayHello(**))")
    private void pointcut1() {
    }

    @Around("pointcut1() ")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {

        Object proxy = pjp.getThis();
        Object target = pjp.getTarget();
        Object[] args = pjp.getArgs();
        LOGGER.info("proxy={}, target={}, args={}", proxy, target, args);
        Object proceed = pjp.proceed(args);
        LOGGER.info("proceed={}", proceed);
        return proceed;

    }

    @AfterThrowing("pointcut1() and args(pjp)")
    public Object onException(ProceededJoinPoint pjp) throws Throwable {
        Throwable throwable = pjp.getThrowable();
        if (throwable != null) {
            LOGGER.error("onException", throwable);
            throw throwable;
        }
        return pjp.getResult();
    }

    @Before("execution(* com.youthlin.aop.test.service.NoItfsService.getName(**))")
    public void before(JoinPoint jp) {
        LOGGER.info("args:{}", jp.getArgs());
    }

}
