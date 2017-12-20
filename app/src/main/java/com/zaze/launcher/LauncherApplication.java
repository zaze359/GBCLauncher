package com.zaze.launcher;

import com.zaze.common.base.BaseApplication;
import com.zaze.launcher.util.LauncherSharePref;

/**
 * Description : LauncherApplication
 *
 * @author : ZAZE
 * @version : 2017-12-18 - 17:15
 */
public class LauncherApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        LauncherSharePref.init();
    }
}
