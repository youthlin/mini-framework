package com.youthlin.ioc.exception;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-10 19:48.
 */
public class NoSuchBeanException extends RuntimeException {
    public NoSuchBeanException() {
        super();
    }

    public NoSuchBeanException(String message) {
        super(message);
    }

    public NoSuchBeanException(String message, Throwable cause) {
        super(message, cause);
    }
}
