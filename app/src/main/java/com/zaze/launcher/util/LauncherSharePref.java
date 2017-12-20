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

    public static void init() {
        ZSharedPrefUtil.setSpName("gbc_launcher");
    }


    public static <T> void apply(String key, @NonNull T value) {
    }

    public static <T> boolean commit(String key, @NonNull T value) {
        return false;
    }

    public static <T> T get(String key, @NonNull T defaultValue) {
        return defaultValue;
    }
}
