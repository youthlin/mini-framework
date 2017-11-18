package com.youthlin.ioc.test.dao;

import com.youthlin.ioc.test.po.Cat;

import javax.annotation.Resource;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:43.
 */
@Resource
public class CatDaoV2 implements ICatDao {
    @Override public void save(Cat cat) {
        //do nothing
    }

    @Override public Cat findByName(String catName) {
        return new Cat().setName(catName);
    }
}
