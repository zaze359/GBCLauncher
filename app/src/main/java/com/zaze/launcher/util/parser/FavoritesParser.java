package com.zaze.launcher.util.parser;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.util.LogTag;
import com.zaze.utils.ZStringUtil;
import com.zaze.utils.log.ZLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-16 - 19:51
 */
class FavoritesParser {
    private static final String TAG_NAME_SPACE = "http://schemas.android.com/apk/res-auto/com.android.launcher3";
    private static final String TAG_INCLUDE = "include";
    /**
     * Attrs for "Include"
     */
    private static final String ATTR_WORKSPACE = "workspace";
    protected static final String ATTR_CONTAINER = "container";
    private static final String ATTR_RANK = "rank";
    protected static final String ATTR_SCREEN = "screen";
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";

    private static final String HOT_SEAT_CONTAINER_NAME = LauncherSettings.ItemColumns.containerToString(LauncherSettings.ItemColumns.CONTAINER_HOT_SEAT);

    protected final Resources mSourceRes;

    private final String mRootTag;
    private final int mHotSeatAllAppsRank;

    private int count;

    private Favorites favorites;
    private final long[] mTemp = new long[2];
    private HashMap<String, TagParser<Favorites>> tagParserMap;

    FavoritesParser(Resources resources, String rootTag, int hotSeatAllAppsRank) {
        mSourceRes = resources;
        this.mRootTag = rootTag;
        favorites = new Favorites();
        count = 0;
        this.mHotSeatAllAppsRank = hotSeatAllAppsRank;
    }


    int parseLayout(int layoutId, ArrayList<Long> screenIds, HashMap<String, TagParser<Favorites>> tagParserMap)
            throws XmlPullParserException, IOException {
        this.tagParserMap = tagParserMap;
        XmlResourceParser parser = mSourceRes.getXml(layoutId);
        count = 0;
        doParser(parser, screenIds, mRootTag);
        final int result = count;
        count = 0;
        return result;
    }

    private int parserDepth = -1;

    private void doParser(XmlResourceParser parser, ArrayList<Long> screenIds, String mRootTag)
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    startDocument(parser, mRootTag);
                    break;
                case XmlPullParser.START_TAG:
//                    if (parserDepth > 0 && parserDepth == parser.getDepth()) {
//                        // 允许解析同级标签
//                        parserDepth = -1;
//                    }
                    if (parserDepth < 0) {
                        startElement(parser, screenIds);
                    }
                    break;
                case XmlPullParser.TEXT:
                    characters(parser);
                    break;
                case XmlPullParser.END_TAG:
                    if (parserDepth > 0 && parserDepth == parser.getDepth()) {
                        // 存在不解析的标签，这个标签结束解析时置负, 允许解析同级标签
                        parserDepth = -1;
                    }
                    endElement(parser);
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        endDocument();
    }

    private void doParser(int layoutId, ArrayList<Long> screenIds, String mRootTag)
            throws XmlPullParserException, IOException {
        doParser(mSourceRes.getXml(layoutId), screenIds, mRootTag);
    }


    /**
     * 开始解析文件, 并处理第一个节点
     *
     * @param parser          parser
     * @param rootElementName 需要解析的根节点, 若为空则不判断是否匹配
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void startDocument(XmlResourceParser parser, String rootElementName)
            throws XmlPullParserException, IOException {
        ZLog.d(LogTag.TAG_DOC, "-----------------------------------");
        ZLog.d(LogTag.TAG_DOC, "开始解析DOC");
        int type = parser.next();
        while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            type = parser.next();
        }
        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }
        String name = parser.getName();
        ZLog.d(LogTag.TAG_DOC, ZStringUtil.format("预处理根节点 : %s(%s)", name, parser.getDepth()));
        if (!TextUtils.isEmpty(rootElementName)) {
            if (!name.equals(rootElementName)) {
                throw new XmlPullParserException("Unexpected start tag: found " + name + ", expected " + rootElementName);
            }
        }
    }

    /**
     * 结束解析文件
     */
    private void endDocument() {
        ZLog.d(LogTag.TAG_DOC, "结束解析DOC");
        ZLog.d(LogTag.TAG_DOC, "-----------------------------------");
    }


    /**
     * 开始解析节点
     * 仅当不处理这个标签时 parserDepth > 0;
     * parserDepth < 0 可以解析, parserDepth > 0, 跳过子标签解析
     * 该标签结束解析时 parserDepth 置为 -1
     * 解析到同级标签的开始时 parserDepth 置为 -1??
     */
    private void startElement(XmlResourceParser parser, ArrayList<Long> screenIds)
            throws XmlPullParserException, IOException {
        String name = parser.getName();
        ZLog.d(LogTag.TAG_DOC, ZStringUtil.format("开始解析Element : %s(%s)", name, parser.getDepth()));
        parserDepth = -1;
        if (TAG_INCLUDE.equals(name)) {
            ZLog.d(LogTag.TAG_DOC, "解析 include 内容");
            final int resId = getAttributeResourceValue(parser, ATTR_WORKSPACE, 0);
            if (resId != 0) {
                try {
                    doParser(resId, screenIds, null);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (tagParserMap.containsKey(name)) {
            TagParser<Favorites> tagParser = tagParserMap.get(name);
            parseContainerAndScreen(parser, mTemp);
            final long container = mTemp[0];
            final long screenId = mTemp[1];
            favorites = new Favorites();
            favorites.setContainer(container);
            favorites.setScreen(screenId);
            favorites.setCellX(ZStringUtil.parseInt(getAttributeValue(parser, ATTR_X)));
            favorites.setCellY(ZStringUtil.parseInt(getAttributeValue(parser, ATTR_Y)));
            long newElementId = tagParser.parseAndAdd(parser, favorites);
            if (newElementId >= 0) {
                // 需要显示到桌面, 记录屏幕id
                if (container == LauncherSettings.ItemColumns.CONTAINER_DESKTOP
                        && !screenIds.contains(screenId)) {
                    screenIds.add(screenId);
                }
                count++;
            }
        } else {
            parserDepth = parser.getDepth();
        }
    }

    /**
     * 结束解析节点
     */
    private void endElement(XmlResourceParser parser) {
        ZLog.d(LogTag.TAG_DOC, ZStringUtil.format("结束解析Element : %s(depth : %s)", parser.getName(), parser.getDepth()));
        ZLog.d(LogTag.TAG_DOC, "-----------------------------------");
    }


    /**
     * 解析内容
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    protected void characters(XmlResourceParser parser) throws IOException, XmlPullParserException {
        ZLog.d(LogTag.TAG_DOC, ZStringUtil.format("内容 : %s(%s)", parser.nextText(), parser.getDepth()));
    }

    // --------------------------------------------------


    /**
     * 根据 rootElementName 进行解析
     *
     * @param parser          parser
     * @param rootElementName rootElementName
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Deprecated
    protected static void beginDocument(XmlPullParser parser, String rootElementName)
            throws XmlPullParserException, IOException {
        ZLog.d(LogTag.TAG_DOC, ZStringUtil.format("开始解析，当前根节点 : %s(%s)", rootElementName, parser.getDepth()));

        int type = parser.next();
        while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            type = parser.next();
        }
        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }
        String name = parser.getName();
        if (!name.equals(rootElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + name + ", expected " + rootElementName);
        }
    }

    /**
     * Parses container and screenId attribute from the current tag, and puts it in the out.
     *
     * @param out array of size 2.
     */
    protected void parseContainerAndScreen(XmlResourceParser parser, long[] out) {
        if (HOT_SEAT_CONTAINER_NAME.equals(getAttributeValue(parser, ATTR_CONTAINER))) {
            // 一般表示(按照一定规范的)外部资源加载到热键中, 会嵌入一个 AllAppsButton, 之后的需要顺移一位
            out[0] = LauncherSettings.ItemColumns.CONTAINER_HOT_SEAT;
            long rank = Long.parseLong(getAttributeValue(parser, ATTR_RANK));
            out[1] = (rank < mHotSeatAllAppsRank) ? rank : (rank + 1);
        } else {
            // 一般表示内部资源
            out[0] = LauncherSettings.ItemColumns.CONTAINER_DESKTOP;
            out[1] = Long.parseLong(getAttributeValue(parser, ATTR_SCREEN));
        }
    }


    // --------------------------------------------------

    /**
     * Return attribute value, attempting launcher-specific namespace first
     * before falling back to anonymous attribute.
     */
    protected static String getAttributeValue(XmlResourceParser parser, String attribute) {
        String value = parser.getAttributeValue(
                "http://schemas.android.com/apk/res-auto/com.android.launcher3", attribute);
        if (value == null) {
            value = parser.getAttributeValue(null, attribute);
        }
        ZLog.d(LogTag.TAG_DOC, "attribute : " + attribute + " : " + value);
        return value;
    }

    /**
     * 解析属性值
     *
     * @param parser       parser
     * @param attribute    attribute
     * @param defaultValue defaultValue
     * @return 属性值
     */
    protected static int getAttributeResourceValue(XmlResourceParser parser, String attribute, int defaultValue) {
        int value = parser.getAttributeResourceValue(TAG_NAME_SPACE, attribute, defaultValue);
        if (value == defaultValue) {
            value = parser.getAttributeResourceValue(null, attribute, defaultValue);
        }
        return value;
    }

    // --------------------------------------------------
}