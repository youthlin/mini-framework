package com.youthlin.aop.test.chain;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 12:15
 */
public interface Handler {
    void handle(Params params, Chain chain) throws Throwable;
}
