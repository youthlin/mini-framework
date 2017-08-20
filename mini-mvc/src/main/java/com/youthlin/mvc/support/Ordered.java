package com.youthlin.mvc.support;

import java.util.Comparator;

/**
 * 有序的对象
 * 创建： youthlin.chen
 * 时间： 2017-08-14 16:06.
 */
public interface Ordered {
    Comparator<Ordered> DEFAULT_ORDERED_COMPARATOR = new Comparator<Ordered>() {
        @Override
        public int compare(Ordered o1, Ordered o2) {
            return o1.getOrder() - o2.getOrder();
        }
    };

    /**
     * 用于排序
     */
    int getOrder();
}
