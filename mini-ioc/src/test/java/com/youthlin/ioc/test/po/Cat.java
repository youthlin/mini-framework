package com.youthlin.ioc.test.po;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:41.
 */
public class Cat {
    private String name;

    @Override public String toString() {
        return "Cat{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public Cat setName(String name) {
        this.name = name;
        return this;
    }
}
