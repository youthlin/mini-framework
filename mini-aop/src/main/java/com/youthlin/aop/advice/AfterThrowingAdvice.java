package com.youthlin.aop.advice;

import com.youthlin.aop.core.JoinPointImpl;

/**
 * 创建: youthlin.chen
 * 时间: 2018-05-03 18:55
 */
public class AfterThrowingAdvice extends AbstractAdvice {
    @Override
    protected void onException(JoinPointImpl point) throws Throwable {
        getAdvisorMethod().invoke(getAdvisor(), buildAdviceMethodArgs(point));
    }

}
