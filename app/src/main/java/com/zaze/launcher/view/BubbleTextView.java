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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.zaze.launcher.DeviceProfile;
import com.zaze.launcher.LauncherAppState;
import com.zaze.launcher.R;
import com.zaze.launcher.data.entity.ShortcutInfo;
import com.zaze.launcher.util.Utilities;

/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class BubbleTextView extends AppCompatTextView {

    private final boolean mLayoutHorizontal;

    private Drawable mIcon;
    private final int mIconSize;

    private int mTextColor;


    public BubbleTextView(Context context) {
        this(context, null, 0);
    }

    public BubbleTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DeviceProfile grid = LauncherAppState.getInstance().getDeviceProfile();
        int defaultIconSize = grid.iconSizePx;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BubbleTextView, defStyleAttr, 0);
        mLayoutHorizontal = a.getBoolean(R.styleable.BubbleTextView_layoutHorizontal, false);
        mIconSize = a.getDimensionPixelSize(R.styleable.BubbleTextView_iconSizeOverride, defaultIconSize);
    }


    @Override
    public void setTextColor(int color) {
        mTextColor = color;
        super.setTextColor(color);
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        mTextColor = colors.getDefaultColor();
        super.setTextColor(colors);
    }


    private Drawable setIcon(Drawable icon, int iconSize) {
        mIcon = icon;
        if (iconSize != -1) {
            mIcon.setBounds(0, 0, iconSize, iconSize);
        }
        if (mLayoutHorizontal) {
            if (Utilities.ATLEAST_JB_MR1) {
                setCompoundDrawablesRelative(mIcon, null, null, null);
            } else {
                setCompoundDrawables(mIcon, null, null, null);
            }
        } else {
            setCompoundDrawables(null, mIcon, null, null);
        }
        return icon;
    }


    public void setTextVisibility(boolean visible) {
        if (visible) {
            super.setTextColor(mTextColor);
        } else {

            super.setTextColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        }
    }

    // --------------------------------------------------

    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache) {
        applyFromShortcutInfo(info, iconCache, false);
    }

    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
                                      boolean promiseStateChanged) {
        Bitmap b = info.getIcon(iconCache);
        // TODO
//        FastBitmapDrawable iconDrawable = mLauncher.createIconDrawable(b);
//        iconDrawable.setGhostModeEnabled(info.isDisabled != 0);
//        setIcon(iconDrawable, mIconSize);
        setIcon(new BitmapDrawable(b), mIconSize);

        if (info.contentDescription != null) {
            setContentDescription(info.contentDescription);
        }


        setText(info.title);
        setTag(info);
        if (promiseStateChanged || info.isPromise()) {
            applyState(promiseStateChanged);
        }
    }

    public void applyState(boolean promiseStateChanged) {
        if (getTag() instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) getTag();
            final boolean isPromise = info.isPromise();
            final int progressLevel = isPromise ?
                    ((info.hasStatusFlag(ShortcutInfo.FLAG_INSTALL_SESSION_ACTIVE) ?
                            info.getInstallProgress() : 0)) : 100;

            if (mIcon != null) {
                // TODO
//                final PreloadIconDrawable preloadDrawable;
//                if (mIcon instanceof PreloadIconDrawable) {
//                    preloadDrawable = (PreloadIconDrawable) mIcon;
//                } else {
//                    preloadDrawable = new PreloadIconDrawable(mIcon, getPreloaderTheme());
//                    setIcon(preloadDrawable, mIconSize);
//                }
//
//                preloadDrawable.setLevel(progressLevel);
//                if (promiseStateChanged) {
//                    preloadDrawable.maybePerformFinishedAnimation();
//                }
            }
        }
    }
}
