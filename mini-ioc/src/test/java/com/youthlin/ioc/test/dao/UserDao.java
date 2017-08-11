package com.youthlin.ioc.test.dao;

import com.youthlin.ioc.annotaion.Bean;
import com.youthlin.ioc.test.po.User;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:38.
 */
@Bean
public class UserDao implements IUserDao {
    @Override public User findById(long id) {
        return new User().setId(id).setName("user " + id);
    }
}
