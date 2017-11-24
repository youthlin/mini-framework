package com.youthlin.ioc.context;

import com.youthlin.ioc.spi.IPostScanner;
import com.youthlin.ioc.spi.IPreScanner;

import java.util.Iterator;
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
        this(preScannerList, null, scanPackages);
    }

    public ClasspathContext(List<IPreScanner> preScannerList, List<IPostScanner> postScannerList,
            String... scanPackages) {
        super(preScannerList, postScannerList, scanPackages);
    }

    public ClasspathContext(Iterator<IPreScanner> preScannerIterator, Iterator<IPostScanner> postScannerIterator,
            String... scanPackages) {
        super(preScannerIterator, postScannerIterator, scanPackages);
    }

}
