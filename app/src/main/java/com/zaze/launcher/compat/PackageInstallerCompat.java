package com.zaze.launcher.compat;

import android.content.Context;

import com.zaze.launcher.util.Utilities;

import java.util.HashMap;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-01 - 09:55
 */
public abstract class PackageInstallerCompat {
    private static final Object sInstanceLock = new Object();
    private static PackageInstallerCompat sInstance;

    public static PackageInstallerCompat getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.ATLEAST_LOLLIPOP) {
                    sInstance = new PackageInstallerCompatVL(context);
                } else {
                    sInstance = new PackageInstallerCompatV16();
                }
            }
            return sInstance;
        }
    }

    /**
     * a map of active installs to their progress
     *
     * @return a map of active installs to their progress
     */
    public abstract HashMap<String, Integer> updateAndGetActiveSessionCache();

    public abstract void onStop();


    public static final class PackageInstallInfo {
        public final String packageName;

        public int state;
        public int progress;

        public PackageInstallInfo(String packageName) {
            this.packageName = packageName;
        }

        public PackageInstallInfo(String packageName, int state, int progress) {
            this.packageName = packageName;
            this.state = state;
            this.progress = progress;
        }
    }
}
