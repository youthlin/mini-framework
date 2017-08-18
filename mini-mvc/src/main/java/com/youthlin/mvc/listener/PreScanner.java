package com.youthlin.mvc.listener;

import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPreScanner;
import com.youthlin.mvc.support.mybatis.MapperScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-18 23:03.
 */
public class PreScanner implements IPreScanner{
    private static final Logger LOGGER = LoggerFactory.getLogger(PreScanner.class);
    @Override
    public void preScan(Context context) {
        try {
            MapperScanner mapperScanner = new MapperScanner();
            mapperScanner.scan(context);
        } catch (Throwable e) {
            LOGGER.debug("", e);
        }
    }
}
