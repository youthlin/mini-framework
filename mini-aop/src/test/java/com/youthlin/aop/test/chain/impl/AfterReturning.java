package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Params;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 13:48
 */
public class AfterReturning extends HandlerAdapter {
    @Override
    protected void onDone(Params params) {
        System.out.println("done. params=" + params);
    }
}
