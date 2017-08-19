package com.youthlin.mvc.listener;

import com.youthlin.mvc.annotation.HttpMethod;

import java.util.Arrays;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 16:03.
 */
@SuppressWarnings("WeakerAccess")
public class URLAndMethods {
    private String url;
    private HttpMethod[] httpMethods;
    public static final HttpMethod[] EMPTY_HTTP_METHODS = new HttpMethod[0];
    public static final HttpMethod[] HTTP_METHODS_GET = new HttpMethod[]{HttpMethod.GET};
    public static final HttpMethod[] HTTP_METHODS_HEAD = new HttpMethod[]{HttpMethod.HEAD};
    public static final HttpMethod[] HTTP_METHODS_POST = new HttpMethod[]{HttpMethod.POST};
    public static final HttpMethod[] HTTP_METHODS_PUT = new HttpMethod[]{HttpMethod.PUT};
    public static final HttpMethod[] HTTP_METHODS_PATCH = new HttpMethod[]{HttpMethod.PATCH};
    public static final HttpMethod[] HTTP_METHODS_DELETE = new HttpMethod[]{HttpMethod.DELETE};
    public static final HttpMethod[] HTTP_METHODS_OPTIONS = new HttpMethod[]{HttpMethod.OPTIONS};
    public static final HttpMethod[] HTTP_METHODS_TRACE = new HttpMethod[]{HttpMethod.TRACE};

    public static HttpMethod[] method(String method) {
        switch (method) {
            case "GET":
                return HTTP_METHODS_GET;
            case "HEAD":
                return HTTP_METHODS_HEAD;
            case "POST":
                return HTTP_METHODS_POST;
            case "PUT":
                return HTTP_METHODS_PUT;
            case "PATCH":
                return HTTP_METHODS_PATCH;
            case "DELETE":
                return HTTP_METHODS_DELETE;
            case "OPTIONS":
                return HTTP_METHODS_OPTIONS;
            case "TRACE":
                return HTTP_METHODS_TRACE;
        }
        return EMPTY_HTTP_METHODS;
    }

    public URLAndMethods(String url, HttpMethod[] httpMethods) {
        this.url = url;
        this.httpMethods = httpMethods;
    }

    @Override
    public String toString() {
        return "URLAndMethods{" +
                "url='" + url + '\'' +
                ", httpMethods=" + Arrays.toString(httpMethods) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        URLAndMethods that = (URLAndMethods) o;

        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(httpMethods, that.httpMethods);
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(httpMethods);
        return result;
    }

    public String getUrl() {

        return url;
    }

    public URLAndMethods setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpMethod[] getHttpMethods() {
        return httpMethods;
    }

    public URLAndMethods setHttpMethods(HttpMethod[] httpMethods) {
        this.httpMethods = httpMethods;
        return this;
    }
}
