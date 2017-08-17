package com.youthlin.mvc.servlet;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-17 22:29
 */
class HttpRequestWithModelMap extends HttpServletRequestFacade implements HttpServletRequest {
    private Map<String, Object> map = new LinkedHashMap<>();

    Map<String, Object> getMap() {
        return map;
    }

    HttpRequestWithModelMap(HttpServletRequest request) {
        super(request);
    }

    @Override
    public void setAttribute(String name, Object o) {
        map.put(name, o);
        super.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        map.remove(name);
        super.removeAttribute(name);
    }

}
