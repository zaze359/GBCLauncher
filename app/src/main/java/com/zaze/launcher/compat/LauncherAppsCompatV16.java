package com.zaze.launcher.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-28 - 09:30
 */
public class LauncherAppsCompatV16 extends LauncherAppsCompat {
    private PackageManager mPm;

    public LauncherAppsCompatV16(Context context) {
        mPm = context.getPackageManager();
    }

    @Override
    public boolean isPackageEnabledForProfile(String packageName, UserHandleCompat user) {
        return isAppEnabled(mPm, packageName, 0);
    }

    @Override
    public boolean isActivityEnabledForProfile(ComponentName component, UserHandleCompat user) {
        try {
            ActivityInfo info = mPm.getActivityInfo(component, 0);
            return info != null && info.isEnabled();
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
