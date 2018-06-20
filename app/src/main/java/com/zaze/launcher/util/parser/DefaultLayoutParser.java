package com.zaze.launcher.util.parser;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.util.LogTag;
import com.zaze.utils.log.ZLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-22 - 16:11
 */
public class DefaultLayoutParser extends AutoInstallsLayout {
    private static final String TAG = "DefaultLayout" + LogTag.TAG_BASE;

    private static final String TAG_FAVORITES = "favorites";
    // --------------------------------------------------

    protected static final String TAG_FAVORITE = "favorite";
    protected static final String TAG_RESOLVE = "resolve";
    private static final String TAG_SHORTCUT = "shortcut";

    protected static final String ATTR_URI = "uri";


    public DefaultLayoutParser(Context context, AppWidgetHost appWidgetHost, LayoutParserCallback callback, Resources targetResources, int layoutId) {
        super(context, appWidgetHost, callback, targetResources, layoutId, TAG_FAVORITES);
    }

    public DefaultLayoutParser(Context context, AppWidgetHost appWidgetHost, LayoutParserCallback callback, Resources res, int layoutId, String rootTag, int hotSeatAllAppsRank) {
        super(context, appWidgetHost, callback, res, layoutId, rootTag, hotSeatAllAppsRank);
    }


    @Override
    protected void parseContainerAndScreen(XmlResourceParser parser, long[] out) {
        out[0] = LauncherSettings.ItemColumns.CONTAINER_DESKTOP;
        String strContainer = getAttributeValue(parser, ATTR_CONTAINER);
        if (strContainer != null) {
            out[0] = Long.valueOf(strContainer);
        }
        out[1] = Long.parseLong(getAttributeValue(parser, ATTR_SCREEN));
    }

    // --------------------------------------------------
    // --------------------------------------------------


    @Override
    protected HashMap<String, TagParser> getFolderElementsMap() {
        return getFolderElementsMap(mSourceRes);
    }

    private HashMap<String, TagParser> getFolderElementsMap(Resources res) {
        HashMap<String, TagParser> parsers = new HashMap<String, TagParser>();
        parsers.put(TAG_FAVORITE, new AppShortcutWithUriParser());
//        parsers.put(TAG_SHORTCUT, new UriShortcutParser(res));
        return parsers;
    }

    @Override
    protected HashMap<String, TagParser<Favorites>> getLayoutElementsMap() {
        HashMap<String, TagParser<Favorites>> parsers = new HashMap<>();
        parsers.put(TAG_FAVORITE, new AppShortcutWithUriParser());
        parsers.put(TAG_APPWIDGET, new AppWidgetParser());
//        parsers.put(TAG_SHORTCUT, new UriShortcutParser(mSourceRes));
        parsers.put(TAG_RESOLVE, new ResolveParser());
//        parsers.put(TAG_FOLDER, new MyFolderParser());
//        parsers.put(TAG_PARTNER_FOLDER, new PartnerFolderParser());
        return parsers;
    }
    // --------------------------------------------------
    // --------------------------------------------------

    class AppShortcutWithUriParser extends AppShortcutParser {
        @Override
        protected long invalidPackageOrClass(XmlResourceParser parser, Favorites values) {
            final String uri = FavoritesParser.getAttributeValue(parser, ATTR_URI);
            if (TextUtils.isEmpty(uri)) {
                ZLog.e(TAG, "跳过无效的 <favorite> : no component or uri");
                return -1;
            }

            final Intent metaIntent;
            try {
                metaIntent = Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                ZLog.e(TAG, "Unable to add meta-favorite: " + uri, e);
                return -1;
            }
            ResolveInfo resolved = mPackageManager.resolveActivity(metaIntent, PackageManager.MATCH_DEFAULT_ONLY);
            final List<ResolveInfo> appList = mPackageManager.queryIntentActivities(
                    metaIntent, PackageManager.MATCH_DEFAULT_ONLY);

            // Verify that the result is an app and not just the resolver dialog asking which app to use.
            // 验证是一个应用, 而不仅仅是解析器对话框要求使用哪个应用程序。
            if (wouldLaunchResolverActivity(resolved, appList)) {
                // 如果只有一个system Resolve 则返回 否则为null
                final ResolveInfo systemApp = getSingleSystemActivity(appList);
                if (systemApp == null) {
                    // There is no logical choice for this meta-favorite, so rather than making
                    // a bad choice just add nothing.
                    Log.w(TAG, "No preference or single system activity found for "
                            + metaIntent.toString());
                    return -1;
                }
                resolved = systemApp;
            }
            final ActivityInfo info = resolved.activityInfo;
            final Intent intent = mPackageManager.getLaunchIntentForPackage(info.packageName);
            if (intent == null) {
                return -1;
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            return addShortcut(info.loadLabel(mPackageManager).toString(), intent,
                    LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION, values);
        }

        /**
         * 如果只有一个system Resolve 则返回 否则为null
         *
         * @param appList appList
         * @return ResolveInfo
         */
        private ResolveInfo getSingleSystemActivity(List<ResolveInfo> appList) {
            ResolveInfo systemResolve = null;
            final int N = appList.size();
            for (int i = 0; i < N; ++i) {
                try {
                    ApplicationInfo info = mPackageManager.getApplicationInfo(
                            appList.get(i).activityInfo.packageName, 0);
                    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        if (systemResolve != null) {
                            return null;
                        } else {
                            systemResolve = appList.get(i);
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "Unable to get info about resolve results", e);
                    return null;
                }
            }
            return systemResolve;
        }

        /**
         * If the list contains the above resolved activity,
         * then it can't be ResolverActivity itself.
         * 如果list已经包含了上述的 resolved activity,则不能解析自己
         *
         * @param resolved resolved
         * @param appList  appList
         * @return boolean
         */
        private boolean wouldLaunchResolverActivity(ResolveInfo resolved, List<ResolveInfo> appList) {
            for (int i = 0; i < appList.size(); ++i) {
                ResolveInfo tmp = appList.get(i);
                if (tmp.activityInfo.name.equals(resolved.activityInfo.name)
                        && tmp.activityInfo.packageName.equals(resolved.activityInfo.packageName)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Contains a list of <favorite> nodes, and accepts the first successfully parsed node.
     */
    protected class ResolveParser implements TagParser<Favorites> {

        private final AppShortcutWithUriParser mChildParser = new AppShortcutWithUriParser();

        @Override
        public long parseAndAdd(XmlResourceParser parser, Favorites values) throws XmlPullParserException, IOException {
            final int groupDepth = parser.getDepth();
            int type;
            long addedId = -1;
            while ((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > groupDepth) {
                if (type != XmlPullParser.START_TAG || addedId > -1) {
                    continue;
                }
                final String fallbackItemName = parser.getName();
                if (TAG_FAVORITE.equals(fallbackItemName)) {
                    addedId = mChildParser.parseAndAdd(parser, values);
                } else {
                    ZLog.e(TAG, "Fallback groups can contain only favorites, found "
                            + fallbackItemName);
                }
            }
            return addedId;
        }
    }

}
