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

package com.zaze.launcher.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.zaze.launcher.InvariantDeviceProfile;
import com.zaze.launcher.compat.LauncherActivityInfoCompat;
import com.zaze.launcher.compat.UserHandleCompat;
import com.zaze.launcher.compat.UserManagerCompat;
import com.zaze.launcher.data.entity.ShortcutInfo;
import com.zaze.launcher.util.ComponentKey;
import com.zaze.launcher.util.Utilities;
import com.zaze.utils.log.ZLog;
import com.zaze.utils.log.ZTag;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    private static final String EMPTY_CLASS_NAME = ".";

    private final HashMap<ComponentKey, CacheEntry> mCache = new HashMap<>(INITIAL_ICON_CACHE_CAPACITY);
    private final HashMap<UserHandleCompat, Bitmap> mDefaultIcons = new HashMap<>();

    private final Context mContext;
    private final PackageManager mPackageManager;
    final UserManagerCompat mUserManager;
    private final int mIconDpi;


    public IconCache(Context context, InvariantDeviceProfile mInvariantDeviceProfile) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mUserManager = UserManagerCompat.getInstance(mContext);
        mIconDpi = mInvariantDeviceProfile.fillResIconDpi;
    }

    // --------------------------------------------------


    private Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), android.R.mipmap.sym_def_app_icon);
    }

    private Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    private Bitmap makeDefaultIcon(UserHandleCompat user) {
        Drawable unbadged = getFullResDefaultActivityIcon();
        Drawable d = mUserManager.getBadgedDrawableForUser(unbadged, user);
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    public Bitmap getDefaultIcon(UserHandleCompat user) {
        if (!mDefaultIcons.containsKey(user)) {
            mDefaultIcons.put(user, makeDefaultIcon(user));
        }
        return mDefaultIcons.get(user);
    }

    public boolean isDefaultIcon(Bitmap icon, UserHandleCompat user) {
        return mDefaultIcons.get(user) == icon;
    }

    public synchronized void getTitleAndIcon(
            ShortcutInfo shortcutInfo, ComponentName component, LauncherActivityInfoCompat info,
            UserHandleCompat user, boolean usePkgIcon, boolean useLowResIcon) {
        CacheEntry entry = cacheLocked(component, info, user, usePkgIcon, useLowResIcon);
        shortcutInfo.setIcon(getNonNullIcon(entry, user));
        shortcutInfo.title = Utilities.trim(entry.title);
        shortcutInfo.usingFallbackIcon = isDefaultIcon(entry.icon, user);
        shortcutInfo.usingLowResIcon = entry.isLowResIcon;
    }

    private Bitmap getNonNullIcon(CacheEntry entry, UserHandleCompat user) {
        return entry.icon == null ? getDefaultIcon(user) : entry.icon;
    }


    /**
     * 从cache中检索entry, 如果这个entry不存在，则创建一个新的entry。
     * 这个方法不是线程安全的，你必须在一个同步方法中调用
     */
    private CacheEntry cacheLocked(ComponentName componentName, LauncherActivityInfoCompat info,
                                   UserHandleCompat user, boolean usePackageIcon, boolean useLowResIcon) {
        ComponentKey cacheKey = new ComponentKey(componentName, user);
        CacheEntry entry = mCache.get(cacheKey);
        if (entry == null || (entry.isLowResIcon && !useLowResIcon)) {
            entry = new CacheEntry();
            mCache.put(cacheKey, entry);
            // 首先检查数据库
            if (!getEntryFromDB(cacheKey, entry, useLowResIcon)) {
                // 数据库中不存在
                if (info != null) {
                    entry.icon = Utilities.createIconBitmap(mContext, info.getBadgedIcon(mIconDpi));
                } else {
                    if (usePackageIcon) {   // 使用应用程序中的icon
                        CacheEntry packageEntry = getEntryForPackageLocked(
                                componentName.getPackageName(), user, false);
                        if (packageEntry != null) {
                        }
                    }
                    if (entry.icon == null) {
                        entry.icon = getDefaultIcon(user);
                    }
                }
            }
            if (TextUtils.isEmpty(entry.title) && info != null) {
                entry.title = info.getLabel();
                entry.contentDescription = mUserManager.getBadgedLabelForUser(entry.title, user);
            }
        }
        return entry;
    }

    private boolean getEntryFromDB(ComponentKey cacheKey, CacheEntry entry, boolean useLowResIcon) {
        // TODO: 2018/5/2  getEntryFromDB
        ZLog.e(ZTag.TAG_DEBUG, "TODO : getEntryFromDB");
        return false;
    }

    /**
     * Adds a default package entry in the cache. This entry is not persisted and will be removed
     * when the cache is flushed.
     */
    public synchronized void cachePackageInstallInfo(String packageName, UserHandleCompat user,
                                                     Bitmap icon, CharSequence title) {
        removeFromMemCacheLocked(packageName, user);

        ComponentKey cacheKey = getPackageKey(packageName, user);
        CacheEntry entry = mCache.get(cacheKey);

        // For icon caching, do not go through DB. Just update the in-memory entry.
        if (entry == null) {
            entry = new CacheEntry();
            mCache.put(cacheKey, entry);
        }
        if (!TextUtils.isEmpty(title)) {
            entry.title = title;
        }
        if (icon != null) {
            entry.icon = Utilities.createIconBitmap(mContext, icon);
        }
    }

    private static ComponentKey getPackageKey(String packageName, UserHandleCompat user) {
        ComponentName cn = new ComponentName(packageName, packageName + EMPTY_CLASS_NAME);
        return new ComponentKey(cn, user);
    }

    /**
     * 为package获取一个entry，作为各种组件的备用entry
     * 这个方法不是线程安全的，必须在同步方法中调用
     */
    private CacheEntry getEntryForPackageLocked(String packageName, UserHandleCompat user,
                                                boolean useLowResIcon) {
        ComponentKey cacheKey = getPackageKey(packageName, user);
        CacheEntry entry = mCache.get(cacheKey);
        if (entry == null || (entry.isLowResIcon && !useLowResIcon)) {
            entry = new CacheEntry();
            boolean entryUpdated = true;
            // Check the DB first.
            if (!getEntryFromDB(cacheKey, entry, useLowResIcon)) {
                try {
                    int flags = UserHandleCompat.myUserHandle().equals(user) ? 0 :
                            PackageManager.GET_UNINSTALLED_PACKAGES;
                    // 获取所有应用 包括有数据但是未安装应用信息
                    PackageInfo info = mPackageManager.getPackageInfo(packageName, flags);
                    ApplicationInfo appInfo = info.applicationInfo;
                    if (appInfo == null) {
                        throw new PackageManager.NameNotFoundException("ApplicationInfo is null");
                    }
                    Drawable drawable = mUserManager.getBadgedDrawableForUser(
                            appInfo.loadIcon(mPackageManager), user);
                    entry.icon = Utilities.createIconBitmap(mContext, drawable);
                    entry.title = appInfo.loadLabel(mPackageManager);
                    entry.contentDescription = mUserManager.getBadgedLabelForUser(entry.title, user);
                    entry.isLowResIcon = false;
                    // Add the icon in the DB here, since these do not get written during
                    // package updates.
                    // TODO 将icon添加到DB。
                    ZLog.e(ZTag.TAG_DEBUG, "TODO : 将icon添加到DB(%s) ", packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    ZLog.w(ZTag.TAG_DEBUG, "Application not installed " + packageName);
                    entryUpdated = false;
                }
            }
            if (entryUpdated) {
                // 添加 缓存中
                mCache.put(cacheKey, entry);
            }
        }
        return entry;
    }


    /**
     * Remove any records for the supplied package name from memory.
     */
    private void removeFromMemCacheLocked(String packageName, UserHandleCompat user) {
        HashSet<ComponentKey> forDeletion = new HashSet<ComponentKey>();
        for (ComponentKey key : mCache.keySet()) {
            if (key.componentName.getPackageName().equals(packageName)
                    && key.user.equals(user)) {
                forDeletion.add(key);
            }
        }
        for (ComponentKey condemned : forDeletion) {
            mCache.remove(condemned);
        }
    }
    // --------------------------------------------------

    static class CacheEntry {
        public Bitmap icon;
        public CharSequence title = "";
        public CharSequence contentDescription = "";
        public boolean isLowResIcon;
    }
}
