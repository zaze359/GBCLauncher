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
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.zaze.launcher.InvariantDeviceProfile;
import com.zaze.launcher.compat.UserHandleCompat;
import com.zaze.launcher.util.ComponentKey;
import com.zaze.launcher.util.Utilities;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    private static final String EMPTY_CLASS_NAME = ".";

    private final HashMap<ComponentKey, CacheEntry> mCache = new HashMap<>(INITIAL_ICON_CACHE_CAPACITY);

    private final Context mContext;



    public IconCache(Context context, InvariantDeviceProfile mInvariantDeviceProfile) {
        mContext = context;

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
            entry.icon = Utilities.createIconBitmap(icon, mContext);
        }
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

    private static ComponentKey getPackageKey(String packageName, UserHandleCompat user) {
        return new ComponentKey(new ComponentName(packageName, packageName + EMPTY_CLASS_NAME), user);
    }

    // --------------------------------------------------

    static class CacheEntry {
        public Bitmap icon;
        public CharSequence title = "";
        public CharSequence contentDescription = "";
        public boolean isLowResIcon;
    }
}
