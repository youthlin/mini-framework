package com.youthlin.ioc.test.service;

import com.youthlin.ioc.annotaion.Bean;
import com.youthlin.ioc.test.dao.ICatDao;
import com.youthlin.ioc.test.dao.IUserDao;
import com.youthlin.ioc.test.po.Cat;
import com.youthlin.ioc.test.po.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:40.
 */
@Bean
public class UserService implements IUserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    @Bean
    private IUserDao userDao;
    @Resource
    private Collection<ICatDao> catDaoList;
    @Resource
    private Set<IUserDao> userDaoSet;
    @Bean
    private Map<String, ICatDao> catDaoMap;
    @Resource
    private HashMap<String, ICatDao> catDaoHashMap = new HashMap<>();
    @Bean
    private Collection<IUserDao> userDaoList = new LinkedHashSet<>();

    @Override public String sayHello(long id) {
        User user = userDao.findById(id);
        LOGGER.debug("userDaoSet: {}", userDaoSet);
        LOGGER.debug("userDaoList: {}", userDaoList);
        return "Hello, " + user.getName();
    }

    @Override public void feedCat(String catName) {
        LOGGER.debug("catDaoMap: {}", catDaoMap);
        LOGGER.debug("catDaoHashMap: {}", catDaoHashMap);
        for (ICatDao catDao : catDaoList) {
            Cat cat = catDao.findByName(catName);
            LOGGER.debug("feed cat: {}", cat);
        }
    }
}
