package com.zaze.launcher.compat;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * Description : 应用启动Activity信息
 *
 * @author : ZAZE
 * @version : 2018-03-16 - 20:17
 */
public abstract class LauncherActivityInfoCompat {

    LauncherActivityInfoCompat() {
    }

    public abstract ComponentName getComponentName();

    public abstract UserHandleCompat getUser();

    public abstract CharSequence getLabel();

    public abstract Drawable getIcon(int density);

    public abstract ApplicationInfo getApplicationInfo();

    public abstract long getFirstInstallTime();

    public abstract Drawable getBadgedIcon(int density);

    /**
     * Creates a LauncherActivityInfoCompat for the primary user.
     */
//    public static LauncherActivityInfoCompat fromResolveInfo(ResolveInfo info, Context context) {
//        return new LauncherActivityInfoCompatV16(context, info);
//    }

}
