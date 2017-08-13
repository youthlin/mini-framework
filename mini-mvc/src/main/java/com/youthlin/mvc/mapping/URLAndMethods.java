package com.youthlin.mvc.mapping;

import com.youthlin.mvc.annotation.Method;

import java.util.Arrays;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 16:03.
 */
public class URLAndMethods {
    private String url;
    private Method[] methods;
    public static final Method[] EMPTY_METHODS = new Method[0];

    public static Method[] method(String method) {
        switch (method) {
        case "GET":
            return new Method[] { Method.GET };
        case "HEAD":
            return new Method[] { Method.HEAD };
        case "POST":
            return new Method[] { Method.POST };
        case "PUT":
            return new Method[] { Method.PUT };
        case "PATCH":
            return new Method[] { Method.PATCH };
        case "DELETE":
            return new Method[] { Method.DELETE };
        case "OPTIONS":
            return new Method[] { Method.OPTIONS };
        case "TRACE":
            return new Method[] { Method.TRACE };
        }
        return EMPTY_METHODS;
    }

    public URLAndMethods(String url, Method[] methods) {
        this.url = url;
        this.methods = methods;
    }

    @Override public String toString() {
        return "URLAndMethods{" +
                "url='" + url + '\'' +
                ", methods=" + Arrays.toString(methods) +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        URLAndMethods that = (URLAndMethods) o;

        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(methods, that.methods);
    }

    @Override public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(methods);
        return result;
    }

    public String getUrl() {

        return url;
    }

    public URLAndMethods setUrl(String url) {
        this.url = url;
        return this;
    }

    public Method[] getMethods() {
        return methods;
    }

    public URLAndMethods setMethods(Method[] methods) {
        this.methods = methods;
        return this;
    }
}
