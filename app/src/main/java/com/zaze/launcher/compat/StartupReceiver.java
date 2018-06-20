package com.zaze.launcher.compat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-06-05 - 13:27
 */
public class StartupReceiver extends BroadcastReceiver {

    public static final String SYSTEM_READY = "com.android.launcher3.SYSTEM_READY";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendStickyBroadcast(new Intent(SYSTEM_READY));
    }
}
