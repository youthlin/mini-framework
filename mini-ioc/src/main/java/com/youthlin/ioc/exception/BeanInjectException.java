package com.youthlin.ioc.exception;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-11 10:17.
 */
public class BeanInjectException extends RuntimeException {
    public BeanInjectException() {
        super();
    }

    public BeanInjectException(String msg) {
        super(msg);
    }

    public BeanInjectException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
