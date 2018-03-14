package com.zaze.launcher.compat;

import java.util.HashMap;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-01 - 10:09
 */
public class PackageInstallerCompatV16 extends PackageInstallerCompat {

    @Override
    public HashMap<String, Integer> updateAndGetActiveSessionCache() {
        return null;
    }

    @Override
    public void onStop() {

    }
}
