package com.youthlin.mvc.view;

import com.youthlin.mvc.support.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 处理 Controller 中被 ResponseBody 修饰的方法的返回值
 * <p>
 * 如需要将对象直接通过 Json 返回给浏览器，
 * 可以自定义一个被 ResponseBody 修饰的注解JsonBody，
 * 然后 accept 方法判断是否含有 JsonBody 注解
 * 并在 handler 方法对返回值序列化为 json 输出至 response
 * <p>
 * 创建： youthlin.chen
 * 时间： 2017-08-14 16:05.
 */
public interface ResponseBodyHandler extends Ordered {
    /**
     * 是否处理调用的返回值
     *
     * @param controllerMethod Controller 中处理请求的方法
     * @return true 表示会处理该方法的返回值
     */
    boolean accept(Method controllerMethod);

    /**
     * 处理 Controller 方法的返回值
     */
    void handler(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception;

}
