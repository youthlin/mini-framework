package com.youthlin.aop.advice;

import com.youthlin.aop.core.JoinPointImpl;

/**
 * 创建: youthlin.chen
 * 时间: 2018-05-03 18:55
 */
public class AfterAdvice extends AbstractAdvice {
    @Override
    protected void after(JoinPointImpl point) throws Throwable {
        getAdvisorMethod().invoke(getAdvisor(), buildAdviceMethodArgs(point));
    }

}
