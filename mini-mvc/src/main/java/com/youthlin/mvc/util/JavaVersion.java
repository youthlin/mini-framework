package com.youthlin.mvc.util;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-18 15:12
 */
public class JavaVersion {
    private static final Boolean supportJava8;

    static {
        boolean java8;
        try {
            Class.forName("java.lang.reflect.Parameter");
            java8 = true;
        } catch (ClassNotFoundException e) {
            java8 = false;
        }
        supportJava8 = java8;
    }

    public static boolean supportJava8() {
        return supportJava8;
    }
}
