package com.zaze.launcher.util.parser;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.zaze.launcher.util.LogTag;
import com.zaze.utils.log.ZLog;

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
    protected HashMap<String, TagParser> getFolderElementsMap() {
        return getFolderElementsMap(mSourceRes);
    }

    HashMap<String, TagParser> getFolderElementsMap(Resources res) {
        HashMap<String, TagParser> parsers = new HashMap<String, TagParser>();
        parsers.put(TAG_FAVORITE, new AppShortcutWithUriParser());
//        parsers.put(TAG_SHORTCUT, new UriShortcutParser(res));
        return parsers;
    }


    // --------------------------------------------------

    class AppShortcutWithUriParser extends AppShortcutParser {
        @Override
        protected long invalidPackageOrClass(XmlResourceParser parser) {
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
            // 验证是一个应用, 而不是一个对话框

            return super.invalidPackageOrClass(parser);
        }

        /**
         * If the list contains the above resolved activity,
         * then it can't be ResolverActivity itself.
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

}
