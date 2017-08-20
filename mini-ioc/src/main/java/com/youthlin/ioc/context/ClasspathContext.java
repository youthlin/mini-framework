package com.youthlin.ioc.context;

import com.youthlin.ioc.spi.IPreScanner;

import java.util.List;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 13:33.
 */
public class ClasspathContext extends AbstractContext {
    public ClasspathContext() {
        super();
    }

    public ClasspathContext(String... scanPackages) {
        super(scanPackages);
    }

    public ClasspathContext(List<IPreScanner> preScannerList) {
        this(preScannerList, "");
    }

    public ClasspathContext(List<IPreScanner> preScannerList, String... scanPackages) {
        super(preScannerList, scanPackages);
    }

}
