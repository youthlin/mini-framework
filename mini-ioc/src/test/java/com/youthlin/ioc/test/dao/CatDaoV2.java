package com.youthlin.ioc.test.dao;

import com.youthlin.ioc.annotaion.Bean;
import com.youthlin.ioc.test.po.Cat;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:43.
 */
@Bean
public class CatDaoV2 implements ICatDao {
    @Override public void save(Cat cat) {
        //do nothing
    }

    @Override public Cat findByName(String catName) {
        return new Cat().setName(catName);
    }
}
