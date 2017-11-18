package com.youthlin.ioc.test.dao;

import com.youthlin.ioc.test.po.User;

import javax.annotation.Resource;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:38.
 */
@Resource
public class UserDao implements IUserDao {
    @Override public User findById(long id) {
        return new User().setId(id).setName("user " + id);
    }
}
