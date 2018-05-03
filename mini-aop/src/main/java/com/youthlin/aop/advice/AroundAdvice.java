package com.youthlin.aop.advice;

import com.youthlin.aop.core.FacadePjp;
import com.youthlin.aop.core.Invocation;
import com.youthlin.aop.core.JoinPointImpl;

/**
 * 创建: youthlin.chen
 * 时间: 2018-05-03 18:55
 */
public class AroundAdvice extends AbstractAdvice {
    @Override
    protected void doInvoke(final JoinPointImpl point, final Invocation invocation) throws Throwable {
        Object[] args = buildAdviceMethodArgs(point);
        if (args.length == 1) {
            args[0] = new FacadePjp(point) {
                @Override
                public Object proceed(Object[] args) throws Throwable {
                    Object result = invocation.invoke(point);
                    point.setResult(result);
                    return result;
                }
            };
        }
        getAdvisorMethod().invoke(getAdvisor(), args);
    }
}
