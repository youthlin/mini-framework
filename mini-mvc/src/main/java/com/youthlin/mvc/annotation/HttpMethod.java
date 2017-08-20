package com.youthlin.mvc.annotation;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 13:44.
 */
public enum HttpMethod {
    GET,//获取资源
    HEAD,//仅输出响应头
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,//获取支持的Method
    TRACE;//回显RequestHeader默认Tomcat会阻断这个Method

    public static HttpMethod fromName(String name) {
        try {
            return HttpMethod.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
