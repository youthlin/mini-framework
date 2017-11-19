package com.youthlin.mvc.listener;

import com.youthlin.mvc.annotation.HttpMethod;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 16:03.
 */
@SuppressWarnings("WeakerAccess")
public class URLAndMethod {
    private String url;
    private HttpMethod httpMethod;

    public URLAndMethod(String url) {
        this(url, null);//for all methods: GET\POST\PUT\...etc
    }

    public URLAndMethod(String url, HttpMethod httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        URLAndMethod that = (URLAndMethod) o;

        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;
        return httpMethod == that.httpMethod;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "URLAndMethod{" +
                "url='" + url + '\'' +
                ", httpMethod=" + httpMethod +
                '}';
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }
}
