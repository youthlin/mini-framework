package com.youthlin.ioc.test;

import com.youthlin.ioc.annotation.Controller;
import com.youthlin.ioc.test.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * 创建: youthlin.chen
 * 时间: 2017-12-05 23:30
 */
@RunWith(MiniRunner.class)
@Scan("com.youthlin.ioc")
@Controller
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Resource
    private UserService userService;

    @Test
    public void test() {
        LOGGER.info("{}{}", this, userService);
        System.out.println(userService.sayHello(1));
    }
}
