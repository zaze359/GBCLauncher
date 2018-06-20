package com.zaze.launcher.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Description : LauncherActivityInfo CompatVL
 *
 * @author : ZAZE
 * @version : 2018-03-16 - 20:17
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LauncherActivityInfoCompatVL extends LauncherActivityInfoCompat {

    private LauncherActivityInfo mLauncherActivityInfo;

    public LauncherActivityInfoCompatVL(LauncherActivityInfo mLauncherActivityInfo) {
        this.mLauncherActivityInfo = mLauncherActivityInfo;
    }

    @Override
    public ComponentName getComponentName() {
        return mLauncherActivityInfo.getComponentName();
    }

    @Override
    public UserHandleCompat getUser() {
        return UserHandleCompat.fromUser(mLauncherActivityInfo.getUser());
    }

    @Override
    public CharSequence getLabel() {
        return mLauncherActivityInfo.getLabel();
    }

    @Override
    public Drawable getIcon(int density) {
        return mLauncherActivityInfo.getIcon(density);
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mLauncherActivityInfo.getApplicationInfo();
    }

    @Override
    public long getFirstInstallTime() {
        return mLauncherActivityInfo.getFirstInstallTime();
    }

    @Override
    public Drawable getBadgedIcon(int density) {
        return mLauncherActivityInfo.getBadgedIcon(density);
    }
}
