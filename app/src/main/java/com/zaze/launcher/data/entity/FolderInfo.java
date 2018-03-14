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

package com.zaze.launcher.data.entity;

import java.util.ArrayList;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends ItemInfo {
    /**
     * The apps and shortcuts
     */
    public ArrayList<ShortcutInfo> contents = new ArrayList<>();
    ArrayList<FolderListener> listeners = new ArrayList<FolderListener>();

    // --------------------------------------------------
    public int options;


    // --------------------------------------------------

    /**
     * Add an app or shortcut
     *
     * @param item
     */
    public void add(ShortcutInfo item) {
        contents.add(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAdd(item);
        }
        itemsChanged();
    }

    void itemsChanged() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onItemsChanged();
        }
    }

    @Override
    public ItemInfo setValues(Favorites favorites) {
        this.title = favorites.getTitle();
        this.options = favorites.getOptions();
        return super.setValues(favorites);
    }

    interface FolderListener {
        void onAdd(ShortcutInfo item);

        void onRemove(ShortcutInfo item);

        void onTitleChanged(CharSequence title);

        void onItemsChanged();
    }
}
