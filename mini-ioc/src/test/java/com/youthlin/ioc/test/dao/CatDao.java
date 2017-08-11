package com.youthlin.ioc.test.dao;

import com.youthlin.ioc.annotaion.Bean;
import com.youthlin.ioc.test.po.Cat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:42.
 */
@Bean
public class CatDao implements ICatDao {
    private Map<String, Cat> map = new ConcurrentHashMap<>();

    @Override public void save(Cat cat) {
        map.put(cat.getName(), cat);
    }

    @Override public Cat findByName(String catName) {
        Cat cat = map.get(catName);
        if (cat == null) {
            cat = new Cat().setName("new Cat");
            save(cat);
        }
        return cat;
    }
}
