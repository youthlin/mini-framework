package com.youthlin.mvc;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-15 23:50
 */
public class NestedServletException extends RuntimeException {
    public NestedServletException() {
        super();
    }

    public NestedServletException(String msg) {
        super(msg);
    }

    public NestedServletException(Throwable cause) {
        super(cause);
    }

    public NestedServletException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
