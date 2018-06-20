package com.zaze.launcher.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Build;

/**
 * Description : LauncherApps LOLLIPOP
 * `
 *
 * @author : ZAZE
 * @version : 2018-02-28 - 09:30
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LauncherAppsCompatVL extends LauncherAppsCompat {

    private LauncherApps mLauncherApps;

    LauncherAppsCompatVL(Context context) {
        super();
        mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    @Override
    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat user) {
        LauncherActivityInfo activity = mLauncherApps.resolveActivity(intent, user.getUser());
        if (activity != null) {
            return new LauncherActivityInfoCompatVL(activity);
        } else {
            return null;
        }
    }

    @Override
    public boolean isPackageEnabledForProfile(String packageName, UserHandleCompat user) {
        return mLauncherApps.isPackageEnabled(packageName, user.getUser());
    }

    @Override
    public boolean isActivityEnabledForProfile(ComponentName component, UserHandleCompat user) {
        return mLauncherApps.isActivityEnabled(component, user.getUser());
    }
}
