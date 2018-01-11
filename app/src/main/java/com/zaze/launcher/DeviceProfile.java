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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * 设备的一些配置
 *
 * @author zaze
 */
public class DeviceProfile {
    public final InvariantDeviceProfile inv;
    // Device properties
    public final boolean isTablet;
    public final boolean isLargeTablet;
    public final boolean isPhone;
    public final boolean transposeLayoutWithOrientation;

    // Device properties in current orientation
    public final boolean isLandscape;

    // Overview mode

    // Workspace
    public final int edgeMarginPx;


    // Workspace icons
    public int iconSizePx;
    public int iconTextSizePx;
    public int iconDrawablePaddingPx;
    public int iconDrawablePaddingOriginalPx;
    public int cellWidthPx;
    public int cellHeightPx;


    // Hot Seat
    public int hotSeatCellWidthPx;
    public int hotSeatCellHeightPx;
    public int hotSeatIconSizePx;
    public int hotSeatBarHeightPx;


    public DeviceProfile(Context context, InvariantDeviceProfile inv,
                         Point minSize, Point maxSize,
                         int width, int height, boolean isLandscape) {
        this.inv = inv;
        this.isLandscape = isLandscape;
        Resources res = context.getResources();
        // resources中的一些常量配置
        isTablet = res.getBoolean(R.bool.is_tablet);
        isLargeTablet = res.getBoolean(R.bool.is_large_tablet);
        isPhone = !isTablet && !isLargeTablet;
        // Some more constants
        transposeLayoutWithOrientation = res.getBoolean(R.bool.hot_seat_transpose_layout_with_orientation);
        edgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);

    }

    public boolean isVerticalBarLayout() {
//        return isLandscape && transposeLayoutWithOrientation;
        return false;
    }


    /**
     * 重新设置图片大小
     *
     * @param icon
     */
    public void resizeIconDrawable(Drawable icon) {
        icon.setBounds(0, 0, iconSizePx, iconSizePx);
    }

    // --------------------------------------------------

    /**
     * 计算Cell宽度
     *
     * @param childWidthSize childWidthSize
     * @param mCountX        mCountX
     * @return Cell宽度
     */
    public static int calculateCellWidth(int childWidthSize, int mCountX) {
        return childWidthSize / mCountX;
    }

    /**
     * 计算Cell高度
     *
     * @param childHeightSize childHeightSize
     * @param mCountY         mCountY
     * @return Cell高度
     */
    public static int calculateCellHeight(int childHeightSize, int mCountY) {
        return childHeightSize / mCountY;
    }

    // --------------------------------------------------

    /**
     * Returns the workspace padding in the specified orientation
     */
    public Rect getWorkspacePadding(boolean isLayoutRtl) {
//        Rect searchBarBounds = getSearchBarBounds(isLayoutRtl);
        Rect padding = new Rect();
//        if (isLandscape && transposeLayoutWithOrientation) {
//            // Pad the left and right of the workspace with search/hotseat bar sizes
//            if (isLayoutRtl) {
//                padding.set(hotseatBarHeightPx, edgeMarginPx,
//                        searchBarBounds.width(), edgeMarginPx);
//            } else {
//                padding.set(searchBarBounds.width(), edgeMarginPx,
//                        hotseatBarHeightPx, edgeMarginPx);
//            }
//        } else {
//            if (isTablet) {
//                // Pad the left and right of the workspace to ensure consistent spacing
//                // between all icons
//                float gapScale = 1f + (dragViewScale - 1f) / 2f;
//                int width = getCurrentWidth();
//                int height = getCurrentHeight();
//                int paddingTop = searchBarBounds.bottom;
//                int paddingBottom = hotseatBarHeightPx + pageIndicatorHeightPx;
//                int availableWidth = Math.max(0, width - (int) ((inv.numColumns * cellWidthPx) +
//                        (inv.numColumns * gapScale * cellWidthPx)));
//                int availableHeight = Math.max(0, height - paddingTop - paddingBottom
//                        - (int) (2 * inv.numRows * cellHeightPx));
//                padding.set(availableWidth / 2, paddingTop + availableHeight / 2,
//                        availableWidth / 2, paddingBottom + availableHeight / 2);
//            } else {
//                // Pad the top and bottom of the workspace with search/hotseat bar sizes
//                padding.set(desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left,
//                        searchBarBounds.bottom,
//                        desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.right,
//                        hotseatBarHeightPx + pageIndicatorHeightPx);
//            }
//        }
        return padding;
    }
}

