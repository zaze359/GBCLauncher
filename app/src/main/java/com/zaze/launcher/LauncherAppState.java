package com.zaze.launcher;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private LauncherAppState(Context context) {
        mInvariantDeviceProfile = new InvariantDeviceProfile();
        // --------------------------------------------------
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        Point smallestSize = new Point();
        Point largestSize = new Point();
        display.getCurrentSizeRange(smallestSize, largestSize);
        // If the partner customization apk contains any grid overrides, apply them
        // Supported overrides: numRows, numColumns, iconSize
//        applyPartnerDeviceProfileOverrides(context, dm);
        Point realSize = new Point();
        display.getRealSize(realSize);
        // The real size never changes. smallSide and largeSide will remain the
        // same in any orientation.
        int smallSide = Math.min(realSize.x, realSize.y);
        int largeSide = Math.max(realSize.x, realSize.y);
        landscapeProfile = new DeviceProfile(context, mInvariantDeviceProfile, smallestSize, largestSize,
                largeSide, smallSide, true);
        portraitProfile = new DeviceProfile(context, mInvariantDeviceProfile, smallestSize, largestSize,
                smallSide, largeSide, false);
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }

    public DeviceProfile getDeviceProfile() {
        return portraitProfile;
    }
}
