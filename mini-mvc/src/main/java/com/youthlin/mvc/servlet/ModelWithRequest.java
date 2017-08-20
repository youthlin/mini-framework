package com.youthlin.mvc.servlet;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-17 22:17
 */
class ModelWithRequest extends LinkedHashMap<String, Object> {
    private HttpServletRequest request;

    ModelWithRequest(HttpServletRequest request) {
        super();
        this.request = request;
        if (request instanceof HttpRequestWithModelMap) {
            this.putAll(((HttpRequestWithModelMap) request).getMap());//when forward a request
        }
    }

    @Override
    public void clear() {
        for (String key : keySet()) {
            request.removeAttribute(key);
        }
        super.clear();
    }

    @Override
    public Object put(String key, Object value) {
        request.setAttribute(key, value);
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        if (m == null) {
            return;
        }
        for (Map.Entry<? extends String, ?> entry : m.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        super.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String)) {
            throw new UnsupportedOperationException("Key should is a String.");
        }
        request.removeAttribute((String) key);
        return super.remove(key);
    }

}
