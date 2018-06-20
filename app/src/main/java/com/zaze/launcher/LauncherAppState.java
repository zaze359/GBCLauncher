package com.zaze.launcher;

import android.content.Context;

import com.zaze.launcher.view.IconCache;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-04 - 22:16
 */
public class LauncherAppState {
    private InvariantDeviceProfile mInvariantDeviceProfile;
    /**
     * 横屏配置
     */
    private DeviceProfile landscapeProfile;

    /**
     * 竖屏配置
     */
    private DeviceProfile portraitProfile;

    private final IconCache mIconCache;

    private static LauncherAppState instance;

    public static LauncherAppState getInstance() {
        if (instance == null) {
            instance = new LauncherAppState(MyApplication.getInstance());
        }
        return instance;
    }

    private LauncherAppState(Context context) {
        mInvariantDeviceProfile = new InvariantDeviceProfile(context);
        landscapeProfile = new DeviceProfile(context, mInvariantDeviceProfile, true);
        portraitProfile = new DeviceProfile(context, mInvariantDeviceProfile, false);
        mIconCache = new IconCache(context, mInvariantDeviceProfile);
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }

    public DeviceProfile getDeviceProfile() {
        return portraitProfile;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }
}
