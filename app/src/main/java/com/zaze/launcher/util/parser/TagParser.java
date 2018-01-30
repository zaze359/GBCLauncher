package com.zaze.launcher.util.parser;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-16 - 13:13
 */
interface TagParser<T> {

    /**
     * 解析标签并且写入db
     *
     * @return the id of the row added or -1;
     */
    long parseAndAdd(XmlResourceParser parser, T values) throws XmlPullParserException, IOException;
}
