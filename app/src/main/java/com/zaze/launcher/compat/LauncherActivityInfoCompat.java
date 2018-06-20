package com.zaze.launcher.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
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

    /**
     * @return ComponentName
     */
    public abstract ComponentName getComponentName();

    /**
     * @return UserHandleCompat
     */
    public abstract UserHandleCompat getUser();

    /**
     * @return 标签
     */
    public abstract CharSequence getLabel();

    /**
     * @param density 屏幕密度
     * @return 图标icon
     */
    public abstract Drawable getIcon(int density);

    /**
     * @return 应用信息
     */
    public abstract ApplicationInfo getApplicationInfo();

    /**
     * @return 首次安装时间
     */
    public abstract long getFirstInstallTime();

    /**
     * @param density 屏幕密度
     * @return
     */
    public abstract Drawable getBadgedIcon(int density);

    /**
     * Creates a LauncherActivityInfoCompat for the primary user.
     */
    public static LauncherActivityInfoCompat fromResolveInfo(ResolveInfo info, Context context) {
        return new LauncherActivityInfoCompatV16(context, info);
    }

}
