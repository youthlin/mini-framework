package com.youthlin.ioc.test.dao;

import com.youthlin.ioc.test.po.User;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:37.
 */
public interface IUserDao {
    User findById(long id);
}
