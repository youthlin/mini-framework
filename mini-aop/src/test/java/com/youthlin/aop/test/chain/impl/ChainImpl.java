package com.youthlin.aop.test.chain.impl;

import com.youthlin.aop.test.chain.Chain;
import com.youthlin.aop.test.chain.Handler;
import com.youthlin.aop.test.chain.Params;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 12:17
 */
public class ChainImpl implements Chain {
    private List<Handler> handlers = new ArrayList<>();
    private int current;
    private Object target;

    @Override
    public void doHandle(Params params) throws Throwable {
        if (current < handlers.size()) {
            handlers.get(current++).handle(params, this);
        } else {
            handle(params);
        }
    }

    public void addHandler(Handler handler) {
        handlers.add(handler);
    }

    private void handle(Params params) {
        System.out.println("invoke target");
        throw new RuntimeException("invoke error");
    }
}
