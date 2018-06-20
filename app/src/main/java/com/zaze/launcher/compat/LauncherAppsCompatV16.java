package com.zaze.launcher.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-28 - 09:30
 */
public class LauncherAppsCompatV16 extends LauncherAppsCompat {
    private PackageManager mPm;
    private Context mContext;
//    private PackageMonitor mPackageMonitor;


    public LauncherAppsCompatV16(Context context) {
        mContext = context;
        mPm = context.getPackageManager();
//        mPackageMonitor = new PackageMonitor();
    }

    @Override
    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat user) {
        ResolveInfo info = mPm.resolveActivity(intent, 0);
        if (info != null) {
            return new LauncherActivityInfoCompatV16(mContext, info);
        }
        return null;
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
