/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaze.launcher;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import android.view.View;

import com.zaze.launcher.util.LauncherSharePref;

class LauncherClings {
    private static final String MIGRATION_CLING_DISMISSED_KEY = "cling_gel.migration.dismissed";
    private static final String WORKSPACE_CLING_DISMISSED_KEY = "cling_gel.workspace.dismissed";
    private Context mContext;

    public LauncherClings(Context context) {
        this.mContext = context;
    }

    /**
     * Shows the migration cling.
     * <p>
     * This flow is mutually exclusive with showFirstRunCling, and only runs if this Launcher
     * package was not preinstalled and there exists a db to migrate from.
     * <p>
     * 和showFirstRunCling是相互排斥的？？
     * 只有在Launcher没有原装并且存在数据库迁移的时候调用？？？
     */
    public void showMigrationCling() {
    }

    private void dismissMigrationCling() {
    }

    public void showLongPressCling(boolean showWelcome) {
    }

    void dismissLongPressCling() {
    }

    /**
     * Hides the specified Cling
     */
    void dismissCling(final View cling, final Runnable postAnimationCb, final String flag, int duration) {
        // To catch cases where siblings of top-level views are made invisible, just check whether
        // the cling is directly set to GONE before dismissing it.
        if (cling != null && cling.getVisibility() != View.GONE) {
            final Runnable cleanUpClingCb = new Runnable() {
                @Override
                public void run() {
                    cling.setVisibility(View.GONE);
                    LauncherSharePref.apply(flag, true);
//                    mIsVisible = false;
                    if (postAnimationCb != null) {
                        postAnimationCb.run();
                    }
                }
            };
            if (duration <= 0) {
                cleanUpClingCb.run();
            } else {
                cling.animate().alpha(0).setDuration(duration).withEndAction(cleanUpClingCb);
            }
        }
    }

    /**
     * Returns whether the clings are enabled or should be shown
     */
    private boolean areClingsEnabled() {
        // disable clings when running in a test harness
        if (ActivityManager.isRunningInTestHarness()) {
            return false;
        }
        // Disable clings for accessibility when explore by touch is enabled
        // TODO: 2017/12/19
        // Restricted secondary users (child mode) will potentially have very few apps
        // seeded when they start up for the first time. Clings won't work well with that
        /// TODO: 2017/12/19  
        if (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.SKIP_FIRST_USE_HINTS, 0) == 1) {
            return false;
        }
        return true;
    }

    /**
     * 是否需要显示第一次运行的一些依附操作
     *
     * @return true显示, false 不显示
     */
    public boolean shouldShowFirstRunOrMigrationClings() {
        return areClingsEnabled() &&
                !LauncherSharePref.get(WORKSPACE_CLING_DISMISSED_KEY, false) &&
                !LauncherSharePref.get(MIGRATION_CLING_DISMISSED_KEY, false);
    }

    /**
     * 同步标记 第一次运行状态为Dismissed
     */
    public static void synchonouslyMarkFirstRunClingDismissed() {
        LauncherSharePref.commit(WORKSPACE_CLING_DISMISSED_KEY, true);
    }
}
