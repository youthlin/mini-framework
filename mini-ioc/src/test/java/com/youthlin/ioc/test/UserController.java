package com.youthlin.ioc.test;

import com.youthlin.ioc.annotation.Controller;
import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.test.service.UserService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
    @Resource
    private Context context;

    @BeforeClass
    public static void beforeClass() {
        LOGGER.info("before class");
    }

    @Before
    public void before() {
        LOGGER.info("before");
    }

    @Test
    public void test1() {
        LOGGER.info("{}", context.getBean(UserController.class) == this);
        System.out.println(userService.sayHello(1));
    }

    @Test
    public void test2() {
        LOGGER.info("{}", context.getBean(UserController.class) == this);
        System.out.println(userService.sayHello(1));
    }

    @Test
    @Ignore
    public void test3() {
        LOGGER.info("{}", context.getBean(UserController.class) == this);
        System.out.println(userService.sayHello(1));
    }

    @After
    public void after() {
        LOGGER.info("after");
    }

    @AfterClass
    public static void afterClass() {
        LOGGER.info("after class");
    }
}
