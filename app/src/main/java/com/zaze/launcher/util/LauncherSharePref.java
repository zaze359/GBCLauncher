package com.zaze.launcher.util;

import android.support.annotation.NonNull;

import com.zaze.utils.ZSharedPrefUtil;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2017-12-19 - 13:35
 */
public class LauncherSharePref {

    public static boolean contains(String key) {
//        return ZSharedPrefUtil.contains(key);
        return false;
    }


    public static <T> void apply(String key, @NonNull T value) {
        ZSharedPrefUtil.apply(key, value);
    }

    public static <T> boolean commit(String key, @NonNull T value) {
        return ZSharedPrefUtil.commit(key, value);
    }

    public static <T> T get(String key, @NonNull T defaultValue) {
        return ZSharedPrefUtil.get(key, defaultValue);
    }
}
