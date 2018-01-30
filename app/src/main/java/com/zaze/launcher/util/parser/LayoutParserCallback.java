package com.zaze.launcher.util.parser;

public interface LayoutParserCallback<T> {

    long insertAndCheck(T values);
}