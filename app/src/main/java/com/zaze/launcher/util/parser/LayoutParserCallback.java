package com.zaze.launcher.util.parser;

/**
 * 布局解析回调
 *
 * @param <T>
 */
public interface LayoutParserCallback<T> {

    /**
     * 检查并插入数据库
     *
     * @param values values
     * @return id
     */
    long insertAndCheck(T values);
}