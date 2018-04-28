package com.youthlin.aop.test.service;

import javax.annotation.Resource;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 17:07
 */
@Resource
public class NoItfsService {
    public String getName(int id) {
        return "No." + id;
    }
}
