package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Params;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 13:16
 */
public class BeforeHandler extends HandlerAdapter {
    @Override
    protected void before(Params params) {
        System.out.println("before. params=" + params);
    }
}
