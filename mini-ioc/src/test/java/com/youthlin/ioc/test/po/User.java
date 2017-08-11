package com.youthlin.ioc.test.po;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:37.
 */
public class User {
    private long id;
    private String name;

    @Override public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public User setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }
}
