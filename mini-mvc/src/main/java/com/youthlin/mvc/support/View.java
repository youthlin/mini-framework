package com.youthlin.mvc.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-17 20:32.
 */
public interface View {
    String getContentType();

    void render(HttpServletRequest request, HttpServletResponse response, Map<String, ?> model) throws Exception;
}
