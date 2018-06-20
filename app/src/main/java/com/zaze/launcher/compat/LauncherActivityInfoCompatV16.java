package com.zaze.launcher.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Description : LauncherActivityInfo CompatVL
 *
 * @author : ZAZE
 * @version : 2018-03-16 - 20:17
 */
public class LauncherActivityInfoCompatV16 extends LauncherActivityInfoCompat {

    private final ResolveInfo mResolveInfo;
    private final ActivityInfo mActivityInfo;
    private final ComponentName mComponentName;
    private final PackageManager mPm;

    LauncherActivityInfoCompatV16(Context context, ResolveInfo info) {
        super();
        mResolveInfo = info;
        mActivityInfo = info.activityInfo;
        mComponentName = new ComponentName(mActivityInfo.packageName, mActivityInfo.name);
        mPm = context.getPackageManager();
    }

    @Override
    public ComponentName getComponentName() {
        return mComponentName;
    }

    @Override
    public UserHandleCompat getUser() {
        return UserHandleCompat.myUserHandle();
    }

    @Override
    public CharSequence getLabel() {
        return mResolveInfo.loadLabel(mPm);
    }

    @Override
    public Drawable getIcon(int density) {
        int iconRes = mResolveInfo.getIconResource();
        Resources resources = null;
        Drawable icon = null;
        // Get the preferred density icon from the app's resources
        if (density != 0 && iconRes != 0) {
            try {
                resources = mPm.getResourcesForApplication(mActivityInfo.applicationInfo);
                icon = resources.getDrawableForDensity(iconRes, density);
            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException exc) {
            }
        }
        // Get the default density icon
        if (icon == null) {
            icon = mResolveInfo.loadIcon(mPm);
        }
        if (icon == null) {
            resources = Resources.getSystem();
            icon = resources.getDrawableForDensity(android.R.mipmap.sym_def_app_icon, density);
        }
        return icon;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mActivityInfo.applicationInfo;
    }

    @Override
    public long getFirstInstallTime() {
        try {
            PackageInfo info = mPm.getPackageInfo(mActivityInfo.packageName, 0);
            return info != null ? info.firstInstallTime : 0;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    @Override
    public Drawable getBadgedIcon(int density) {
        return getIcon(density);
    }

    public String getName() {
        return mActivityInfo.name;
    }

}
