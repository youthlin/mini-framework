package com.youthlin.ioc.exception;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-11 09:55.
 */
public class BeanDefinitionException extends RuntimeException {
    public BeanDefinitionException() {
        super();
    }

    public BeanDefinitionException(String message) {
        super(message);
    }

    public BeanDefinitionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
