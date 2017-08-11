package com.youthlin.ioc.test;

import com.youthlin.ioc.context.ClasspathContext;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.test.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 13:37.
 */
public class Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        Context context = new ClasspathContext();
        IUserService userService = context.getBean(IUserService.class);
        LOGGER.debug("say hello: {}", userService.sayHello(1L));
        userService.feedCat("tom");
        LOGGER.debug("----------------------------------------------");
        context = new ClasspathContext("", "org");
        LOGGER.debug("beans: {}", context.getBeans());
        LOGGER.debug("unloaded class: {}", context.getUnloadedClass());

    }
}
