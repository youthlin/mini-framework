package com.youthlin.aop.test.service;

import com.youthlin.ioc.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建: youthlin.chen
 * 时间: 2018-01-28 19:41
 */
@Service
public class HelloService implements IHelloService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloService.class);

    @Override
    public String sayHello(String name) {
        LOGGER.info("say hello {}", name);
        throw new RuntimeException(name);
        //return "Hello, " + name;
    }

}
