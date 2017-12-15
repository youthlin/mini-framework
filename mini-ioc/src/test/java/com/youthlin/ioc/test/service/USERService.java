package com.youthlin.ioc.test.service;

import com.youthlin.ioc.annotation.Service;
import com.youthlin.ioc.test.dao.ICatDao;
import com.youthlin.ioc.test.dao.IUserDao;
import com.youthlin.ioc.test.po.Cat;
import com.youthlin.ioc.test.po.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 20:40.
 */
@Service("userService")
public class USERService implements IUserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(USERService.class);
    @Resource
    private IUserDao userDao;
    @Resource
    private Collection<ICatDao> catDaoList;
    @Resource
    private Set<IUserDao> userDaoSet;
    @Resource
    private Map<String, ICatDao> catDaoMap;
    @Resource
    private HashMap<String, ICatDao> catDaoHashMap = new HashMap<>();
    @Resource
    private Collection<IUserDao> userDaoList = new LinkedHashSet<>();

    @Override
    public String sayHello(long id) {
        User user = userDao.findById(id);
        LOGGER.debug("userDaoSet: {}", userDaoSet);
        LOGGER.debug("userDaoList: {}", userDaoList);
        return "Hello, " + user.getName();
    }

    @Override
    public void feedCat(String catName) {
        LOGGER.debug("catDaoMap: {}", catDaoMap);
        LOGGER.debug("catDaoHashMap: {}", catDaoHashMap);
        for (ICatDao catDao : catDaoList) {
            Cat cat = catDao.findByName(catName);
            LOGGER.debug("feed cat: {}", cat);
        }
    }
}
