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
import android.util.DisplayMetrics;

import com.zaze.launcher.util.Utilities;

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
    public final int widthPx;
    public final int heightPx;
    public final int availableWidthPx;
    public final int availableHeightPx;

    // Overview mode
    // Workspace
    /**
     *
     */
    public final int edgeMarginPx;
    // Workspace icons
    /**
     *
     */
    public int iconSizePx;
    public int iconTextSizePx;
    public int iconDrawablePaddingPx;
    /**
     * iconDrawable初始padding
     */
    public int iconDrawablePaddingOriginalPx;
    //
    public int cellWidthPx;
    public int cellHeightPx;
    // Hot Seat
    /**
     *
     */
    public int hotSeatCellWidthPx;
    public int hotSeatCellHeightPx;
    public int hotSeatIconSizePx;
    public int hotSeatBarHeightPx;

    // All apps
    /**
     *
     */
    public int allAppsNumCols;
    public int allAppsNumPredictiveCols;

    /**
     * allAppsButton 视觉上的大小
     * 动画结束后的大小
     */
    public int allAppsButtonVisualSize;
//    public final int allAppsIconSizePx;
//    public final int allAppsIconTextSizePx;


    public DeviceProfile(Context context, InvariantDeviceProfile inv, boolean isLandscape) {
        this.inv = inv;
        this.isLandscape = isLandscape;
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        // resources中的一些常量配置
        isTablet = res.getBoolean(R.bool.is_tablet);
        isLargeTablet = res.getBoolean(R.bool.is_large_tablet);
        isPhone = !isTablet && !isLargeTablet;
        // Some more constants
        transposeLayoutWithOrientation = res.getBoolean(R.bool.hot_seat_transpose_layout_with_orientation);
        edgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
//        desiredWorkspaceLeftRightMarginPx = 2 * edgeMarginPx;
//        pageIndicatorHeightPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_page_indicator_height);
//        defaultPageSpacingPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_workspace_page_spacing);
//        overviewModeMinIconZoneHeightPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_min_icon_zone_height);
//        overviewModeMaxIconZoneHeightPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_max_icon_zone_height);
//        overviewModeBarItemWidthPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_item_width);
//        overviewModeBarSpacerWidthPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_spacer_width);
//        overviewModeIconZoneRatio = res.getInteger(R.integer.config_dynamic_grid_overview_icon_zone_percentage) / 100f;

        iconDrawablePaddingOriginalPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_icon_drawable_padding);
        // 处理实际可用的宽高
        // 横屏 : 宽度去掉状态栏
        // 竖屏 : 高度去掉状态栏
        Point realSize = inv.getRealSize();
        if (isLandscape) {
            widthPx = Math.max(realSize.x, realSize.y);
            heightPx = Math.min(realSize.x, realSize.y);
            availableWidthPx = inv.getLargestSize().x;
            availableHeightPx = inv.getSmallestSize().y;
        } else {
            widthPx = Math.min(realSize.x, realSize.y);
            heightPx = Math.max(realSize.x, realSize.y);
            availableWidthPx = inv.getSmallestSize().x;
            availableHeightPx = inv.getLargestSize().y;
        }
        // 计算其他尺寸
        updateAvailableDimensions(dm, res);
        computeAllAppsButtonSize(res);
    }

    /**
     * 检查一下是否需要进行缩放
     *
     * @param dm  DisplayMetrics
     * @param res Resources
     */
    private void updateAvailableDimensions(DisplayMetrics dm, Resources res) {
        float scale = 1f;
        int drawablePadding = iconDrawablePaddingOriginalPx;
        // 需要使用的高度
        float usedHeight = (cellHeightPx * inv.numRows);
        // 最大高度 = 可用高度 - 工作区上下padding
        Rect workspacePadding = getWorkspacePadding(false);
        int maxHeight = (availableHeightPx - workspacePadding.top - workspacePadding.bottom);
        // 需要使用的高度小于 能提供的最大高度, 计算得到缩放比，并且将drawablePadding置为0
        if (usedHeight > maxHeight) {
            scale = maxHeight / usedHeight;
            drawablePadding = 0;
        }
        updateIconSize(scale, drawablePadding, res, dm);
    }

    /**
     * 根据视觉效果,对allAppsButton的大小 进行缩放
     */
    private void computeAllAppsButtonSize(Resources res) {
        float padding = res.getInteger(R.integer.config_allAppsButtonPaddingPercent) / 100f;
        allAppsButtonVisualSize = (int) (hotSeatIconSizePx * (1 - padding));
    }

    /**
     * 更新Icon、HotSeat、Search Bar、Folder等尺寸,
     *
     * @param scale           缩放比
     * @param drawablePadding drawablePadding
     * @param res             Resources
     * @param dm              DisplayMetrics
     */
    private void updateIconSize(float scale, int drawablePadding, Resources res, DisplayMetrics dm) {
        iconSizePx = (int) (Utilities.pxFromDp(inv.iconSize, dm) * scale);
        iconTextSizePx = (int) (Utilities.pxFromSp(inv.iconTextSize, dm) * scale);
        iconDrawablePaddingPx = drawablePadding;
        // HotSeat
        hotSeatIconSizePx = (int) (Utilities.pxFromDp(inv.hotSeatIconSize, dm) * scale);
        hotSeatBarHeightPx = iconSizePx + 4 * edgeMarginPx;
        hotSeatCellWidthPx = iconSizePx;
        hotSeatCellHeightPx = iconSizePx;
        // Search Bar
        // Calculate the actual text height
        // Folder
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

