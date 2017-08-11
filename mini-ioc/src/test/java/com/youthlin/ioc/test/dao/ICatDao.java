package com.youthlin.ioc.test.dao;

import com.youthlin.ioc.test.po.Cat;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:41.
 */
public interface ICatDao {
    void save(Cat cat);

    Cat findByName(String catName);
}
