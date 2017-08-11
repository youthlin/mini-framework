package com.youthlin.ioc.context;

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

}
