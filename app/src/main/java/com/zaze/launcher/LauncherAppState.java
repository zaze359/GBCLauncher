package com.zaze.launcher;

import android.content.Context;

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

    private static LauncherAppState instance;

    public static LauncherAppState getInstance(Context context) {
        if (instance == null) {
            instance = new LauncherAppState(context.getApplicationContext());
        }
        return instance;
    }

    private LauncherAppState(Context context) {
        mInvariantDeviceProfile = new InvariantDeviceProfile(context);
        landscapeProfile = new DeviceProfile(context, mInvariantDeviceProfile, true);
        portraitProfile = new DeviceProfile(context, mInvariantDeviceProfile, false);
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }

    public DeviceProfile getDeviceProfile() {
        return portraitProfile;
    }
}
