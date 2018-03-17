package com.zaze.launcher.util;

import android.animation.AnimatorSet;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-16 - 13:40
 */
public class LauncherAnimUtils {
    public static AnimatorSet createAnimatorSet() {
        AnimatorSet anim = new AnimatorSet();
//        cancelOnDestroyActivity(anim);
        return anim;
    }
}
