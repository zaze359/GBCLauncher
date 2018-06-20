package com.zaze.launcher.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.util.SparseArray;

import com.zaze.launcher.LauncherAppState;
import com.zaze.launcher.view.IconCache;

import java.util.HashMap;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-01 - 10:09
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PackageInstallerCompatVL extends PackageInstallerCompat {

    final SparseArray<String> mActiveSessions = new SparseArray<>();
    final PackageInstaller mInstaller;
    private final IconCache mCache;


    public PackageInstallerCompatVL(Context context) {
        mInstaller = context.getPackageManager().getPackageInstaller();
        mCache = LauncherAppState.getInstance().getIconCache();
    }

    @Override
    public HashMap<String, Integer> updateAndGetActiveSessionCache() {
        HashMap<String, Integer> activePackages = new HashMap<>();
        UserHandleCompat user = UserHandleCompat.myUserHandle();
        for (PackageInstaller.SessionInfo info : mInstaller.getAllSessions()) {
            addSessionInfoToCahce(info, user);
            if (info.getAppPackageName() != null) {
                activePackages.put(info.getAppPackageName(), (int) (info.getProgress() * 100));
                mActiveSessions.put(info.getSessionId(), info.getAppPackageName());
            }
        }
        return activePackages;
    }

    void addSessionInfoToCahce(PackageInstaller.SessionInfo info, UserHandleCompat user) {
        String packageName = info.getAppPackageName();
        if (packageName != null) {
            mCache.cachePackageInstallInfo(packageName, user, info.getAppIcon(), info.getAppLabel());
        }
    }

    @Override
    public void onStop() {

    }
}
