package com.zaze.launcher.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.zaze.launcher.util.Utilities;

/**
 * Description : launcher app 的一些 兼容处理
 *
 * @author : ZAZE
 * @version : 2018-02-28 - 09:30
 */
public abstract class LauncherAppsCompat {

    private static LauncherAppsCompat sInstance;
    private final static Object sInstanceLock = new Object();

    public static LauncherAppsCompat getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.ATLEAST_LOLLIPOP) {
                    sInstance = new LauncherAppsCompatVL(context.getApplicationContext());
                } else {
                    sInstance = new LauncherAppsCompatV16(context.getApplicationContext());
                }
            }
            return sInstance;
        }
    }


    /**
     * 解析intent
     *
     * @param intent intent
     * @param user   user
     * @return LauncherActivityInfoCompat
     */
    public abstract LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat user);


    /**
     * package是否可用
     *
     * @param packageName packageName
     * @param user        user
     * @return package是否可用
     */
    public abstract boolean isPackageEnabledForProfile(String packageName, UserHandleCompat user);

    /**
     * Activity是否可用
     *
     * @param component component
     * @param user      user
     * @return Activity是否可用
     */
    public abstract boolean isActivityEnabledForProfile(ComponentName component, UserHandleCompat user);

    /**
     * 应用是否可用
     *
     * @param pm          pm
     * @param packageName packageName
     * @param flags       flags
     * @return 是否可用
     */
    public boolean isAppEnabled(PackageManager pm, String packageName, int flags) {
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, flags);
            return info != null && info.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
