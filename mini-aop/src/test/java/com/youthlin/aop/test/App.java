package com.youthlin.aop.test;

import com.youthlin.aop.test.service.IHelloService;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.test.MiniRunner;
import com.youthlin.ioc.test.Scan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * 创建: youthlin.chen
 * 时间: 2018-01-28 19:39
 */
@RunWith(MiniRunner.class)
@Scan("com.youthlin.aop.test")
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    @Resource
    private Context context;
    @Resource
    private IHelloService helloService;

    @Test
    public void run() {
        LOGGER.info("{}", context.getNameBeanMap());
        LOGGER.info("sayHello: {}", helloService.sayHello("Lin"));
    }

}
