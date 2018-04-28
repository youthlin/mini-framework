package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Params;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 13:45
 */
public class AfterThrowing extends HandlerAdapter {
    @Override
    protected void onException(Params params) throws Throwable {
        params.getThrowable().printStackTrace();
    }
}
