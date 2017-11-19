package com.youthlin.mvc.util;

/**
 * 创建：youthlin.chen
 * 时间：2017-08-17 23:21
 */
@SuppressWarnings("WeakerAccess")
public class Constants {
    // URL开头结尾:
    public static final String FORWARD_CHAR = "/";
    // 用于往ServletContext里添加属性:
    public static final String CONTAINER = "_MINI_IOC_CONTAINER";
    public static final String URL_MAPPING_MAP = "_URL_MAPPING_MAP";
    public static final String MAPPED_URL_SET = "_MAPPED_URL_SET";
    public static final String VIEW_PREFIX = "_VIEW_PREFIX";
    public static final String VIEW_SUFFIX = "_VIEW_SUFFIX";
    // initParameter 名称:
    public static final String VIEW_PREFIX_PARAM_NAME = "view-prefix";
    public static final String VIEW_SUFFIX_PARAM_NAME = "view-suffix";
    // Controller 中返回值:
    public static final String REDIRECT = "redirect:";
    public static final String FORWARD = "forward:";
    // URL 中最后一个点:
    public static final String DOT = ".";
    // mybatis:
    public static final String MYBATIS_SCAN_ANNOTATION = "mybatis-scan-annotation";
    public static final String MYBATIS_SCAN_PACKAGES = "mybatis-scan-packages";
    public static final String MYBATIS_CONFIG_FILE = "mybatis-config-file";
    public static final String MYBATIS_INIT_SQL = "mybatis-init-sql";
    public static final String MYBATIS_INIT_FILE = "mybatis-init-file";
    // thymeleaf:
    public static final String TH_VIEW_PREFIX = "th-prefix";
    public static final String TH_VIEW_SUFFIX = "th-suffix";

}
