package com.youthlin.aop.test.chain.impl;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 12:19
 */
public class ChainTest {
    public static void main(String[] args) throws Throwable {
        ChainImpl chain = new ChainImpl();
        chain.addHandler(new BeforeHandler());
        chain.addHandler(new AfterHandler());
        chain.addHandler(new AfterReturning());
        chain.addHandler(new AfterThrowing());
        Pjp params = new Pjp();
        params.chain = chain;
        chain.doHandle(params);
    }
}
