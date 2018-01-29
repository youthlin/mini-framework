package com.youthlin.aop.test.service;

import com.youthlin.ioc.annotation.Service;

/**
 * 创建: youthlin.chen
 * 时间: 2018-01-28 19:41
 */
@Service
public class HelloService implements IHelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }

}
