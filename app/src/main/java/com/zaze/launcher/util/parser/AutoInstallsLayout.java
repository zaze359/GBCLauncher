package com.zaze.launcher.util.parser;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetHost;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.text.TextUtils;

import com.zaze.launcher.InvariantDeviceProfile;
import com.zaze.launcher.LauncherAppState;
import com.zaze.launcher.LauncherApplication;
import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.util.LogTag;
import com.zaze.launcher.util.Utilities;
import com.zaze.utils.log.ZLog;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Description : Layout parsing code for auto installs layout
 *
 * @author : ZAZE
 * @version : 2018-01-16 - 18:35
 */
public class AutoInstallsLayout {
    private static final String FORMATTED_LAYOUT_RES_WITH_HOSTEAT = "default_layout_%dx%d_h%s";
    private static final String FORMATTED_LAYOUT_RES = "default_layout_%dx%d";
    private static final String LAYOUT_RES = "default_layout";
    //
    private static final String TAG_WORKSPACE = "workspace";
    private static final String TAG_APP_ICON = "appicon";
    private static final String TAG_AUTO_INSTALL = "autoinstall";
    private static final String TAG_FOLDER = "folder";
    private static final String TAG_APPWIDGET = "appwidget";
    private static final String TAG_SHORTCUT = "shortcut";
    private static final String TAG_EXTRA = "extra";


    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_CLASS_NAME = "className";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_SPAN_X = "spanX";
    private static final String ATTR_SPAN_Y = "spanY";
    private static final String ATTR_ICON = "icon";
    private static final String ATTR_URL = "url";

    private static final String RESTRICTION_PACKAGE_NAME = "workspace.configuration.package.name";

    private FavoritesParser layoutParser;

    // --------------------------------------------------
    final Context mContext;
    final AppWidgetHost mAppWidgetHost;
    protected final LayoutParserCallback<Favorites> mCallback;

    protected final PackageManager mPackageManager;
    protected final int mLayoutId;

    public AutoInstallsLayout(Context context, AppWidgetHost appWidgetHost,
                              LayoutParserCallback callback, Resources targetResources,
                              int layoutId, String tagWorkspace) {
        this(context, appWidgetHost, callback, targetResources, layoutId, tagWorkspace,
                LauncherAppState.getInstance(context).getInvariantDeviceProfile().hotSeatAllAppsRank);
    }

    public AutoInstallsLayout(Context context, AppWidgetHost appWidgetHost,
                              LayoutParserCallback callback, Resources res,
                              int layoutId, String rootTag, int hotSeatAllAppsRank) {
        mContext = context;
        mAppWidgetHost = appWidgetHost;
        mCallback = callback;

        mPackageManager = context.getPackageManager();
        mLayoutId = layoutId;
        layoutParser = new FavoritesParser(res, rootTag, hotSeatAllAppsRank);
    }

    public static AutoInstallsLayout newInstance(Context context, String packageName, AppWidgetHost appWidgetHost, LayoutParserCallback callback) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        InvariantDeviceProfile grid = LauncherAppState.getInstance(context).getInvariantDeviceProfile();
        try {
            Resources targetResources = context.getPackageManager().getResourcesForApplication(packageName);
            // 尝试加载包含grid size 和 hotSeat的布局
            String layoutName = String.format(Locale.ENGLISH, FORMATTED_LAYOUT_RES_WITH_HOSTEAT,
                    grid.numColumns, grid.numRows, grid.numHotSeatIcons);
            int layoutId = targetResources.getIdentifier(layoutName, "xml", packageName);
            if (layoutId == 0) {
                // 尝试加载只有grid size 没有hotSeat的布局
                ZLog.e(LogTag.TAG_LAYOUT, "Formatted layout: " + layoutName + " not found. Trying layout without hotSeat");
                layoutName = String.format(Locale.ENGLISH, FORMATTED_LAYOUT_RES, grid.numColumns, grid.numRows);
                layoutId = targetResources.getIdentifier(layoutName, "xml", packageName);
            }
            // 尝试加载默认布局
            if (layoutId == 0) {
                ZLog.e(LogTag.TAG_LAYOUT, "Formatted layout: " + layoutName + " not found. Trying the default layout");
                layoutId = targetResources.getIdentifier(LAYOUT_RES, "xml", packageName);
            }
            if (layoutId == 0) {
                ZLog.e(LogTag.TAG_LAYOUT, "Layout definition not found in package: " + packageName);
                return null;
            }
            return new AutoInstallsLayout(context, appWidgetHost, callback, targetResources, layoutId, TAG_WORKSPACE);
        } catch (PackageManager.NameNotFoundException e) {
            ZLog.e(LogTag.TAG_LAYOUT, "Target package for restricted profile not found", e);
            return null;
        }
    }

    /**
     * 构建一个工作区从规定条例中加载应用
     *
     * @param appWidgetHost appWidgetHost
     * @param callback      callback
     * @return AutoInstallsLayout
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static AutoInstallsLayout createWorkspaceLoaderFromAppRestriction(AppWidgetHost appWidgetHost, LayoutParserCallback callback) {
        // UserManager.getApplicationRestrictions() requires minSdkVersion >= 18
        if (!Utilities.ATLEAST_JB_MR2) {
            return null;
        }
        Context context = LauncherApplication.getInstance();
        UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        Bundle bundle = um.getApplicationRestrictions(context.getPackageName());
        if (bundle == null) {
            return null;
        }
        String packageName = bundle.getString(RESTRICTION_PACKAGE_NAME);
        return AutoInstallsLayout.newInstance(context, packageName, appWidgetHost, callback);
    }

    public int loadLayout(ArrayList<Long> screenIds) {
        try {
            return layoutParser.parseLayout(mLayoutId, screenIds, getLayoutElementsMap());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    protected HashMap<String, TagParser> getFolderElementsMap() {
        HashMap<String, TagParser> parsers = new HashMap<>();
        parsers.put(TAG_APP_ICON, new AppShortcutParser());
        parsers.put(TAG_AUTO_INSTALL, new AutoInstallParser());
//        parsers.put(TAG_SHORTCUT, new ShortcutParser(mSourceRes));
        return parsers;
    }

    protected HashMap<String, TagParser<Favorites>> getLayoutElementsMap() {
        HashMap<String, TagParser<Favorites>> parsers = new HashMap<>();
        parsers.put(TAG_APP_ICON, new AppShortcutParser());
        parsers.put(TAG_AUTO_INSTALL, new AutoInstallParser());
        parsers.put(TAG_FOLDER, new FolderParser());
//        parsers.put(TAG_SHORTCUT, new ShortcutParser(mSourceRes));
        parsers.put(TAG_APPWIDGET, new AppWidgetParser());
        return parsers;
    }


    protected long addShortcut(String title, Intent intent, int type, Favorites values) {
        values.setIntent(intent.toUri(0));
        values.setTitle(title);
        values.setItemType(type);
        values.setSpanX(1);
        values.setSpanY(1);
        return mCallback.insertAndCheck(values);
    }

    // --------------------------------------------------
    // --------------------------------------------------

    /**
     * App shortcuts: required attributes packageName and className
     */
    protected class AppShortcutParser implements TagParser<Favorites> {

        @Override
        public long parseAndAdd(XmlResourceParser parser, Favorites values) throws XmlPullParserException, IOException {
            final String packageName = FavoritesParser.getAttributeValue(parser, ATTR_PACKAGE_NAME);
            final String className = FavoritesParser.getAttributeValue(parser, ATTR_CLASS_NAME);
            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
                try {
                    ComponentName cn;
                    ActivityInfo activityInfo;
                    try {
                        cn = new ComponentName(packageName, className);
                        activityInfo = mPackageManager.getActivityInfo(cn, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        String[] packages = mPackageManager.currentToCanonicalPackageNames(
                                new String[]{packageName});
                        cn = new ComponentName(packages[0], className);
                        activityInfo = mPackageManager.getActivityInfo(cn, 0);
                    }
                    final Intent intent = new Intent(Intent.ACTION_MAIN, null)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setComponent(cn)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    return addShortcut(activityInfo.loadLabel(mPackageManager).toString(),
                            intent, LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, values);
                } catch (PackageManager.NameNotFoundException e) {
                    ZLog.e(LogTag.TAG_ERROR, "Unable to add favorite: " + packageName + "/" + className, e);
                    return -1;
                }
            } else {
                return invalidPackageOrClass(parser);
            }
        }

        /**
         * Helper method to allow extending the parser capabilities
         */
        protected long invalidPackageOrClass(XmlResourceParser parser) {
            ZLog.w(LogTag.TAG_ERROR, "Skipping invalid <favorite> with no component");
            return -1;
        }
    }

    /**
     * AutoInstall: required attributes packageName and className
     */
    protected class AutoInstallParser implements TagParser<Favorites> {

        @Override
        public long parseAndAdd(XmlResourceParser parser, Favorites values) throws XmlPullParserException, IOException {
            return 0;
        }
    }

    protected class FolderParser implements TagParser<Favorites> {

        @Override
        public long parseAndAdd(XmlResourceParser parser, Favorites values) throws XmlPullParserException, IOException {
            return 0;
        }
    }

    /**
     * Parses a web shortcut. Required attributes url, icon, title
     */
    protected class ShortcutParser implements TagParser<Favorites> {
        @Override
        public long parseAndAdd(XmlResourceParser parser, Favorites values) throws XmlPullParserException, IOException {
            return 0;
        }
    }

    /**
     * AppWidget parser: Required attributes packageName, className, spanX and spanY.
     * Options child nodes: <extra key=... value=... />
     */
    protected class AppWidgetParser implements TagParser<Favorites> {
        @Override
        public long parseAndAdd(XmlResourceParser parser, Favorites values) throws XmlPullParserException, IOException {
            return 0;
        }
    }
}
