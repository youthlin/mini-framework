package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Params;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 13:21
 */
public class AfterHandler extends HandlerAdapter {
    @Override
    protected void after(Params params) {
        System.out.println("after. params=" + params);
    }
}
