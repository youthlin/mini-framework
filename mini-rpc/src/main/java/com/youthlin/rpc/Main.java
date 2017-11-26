package com.youthlin.rpc;

import com.youthlin.rpc.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 14:36
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.debug("Hello, World!");
        LOGGER.info("{}", NetUtil.getLocalAddress());

    }
}
