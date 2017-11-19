package com.youthlin.ioc.annotation;

import com.youthlin.ioc.context.Context;

/**
 * 注解处理器.
 * 创建： youthlin.chen
 * 时间： 2017-08-11 09:48.
 */
public interface IAnnotationProcessor {
    void autoScan(Context context, String... scanPackages);

}
