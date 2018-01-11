package com.zaze.launcher.view;

import android.app.WallpaperManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.zaze.launcher.DeviceProfile;
import com.zaze.launcher.LauncherAppState;

/**
 * Description : app快捷键 和 app组件 容器
 *
 * @author : ZAZE
 * @version : 2017-12-25 - 00:22
 */
public class ShortcutAndWidgetContainer extends ViewGroup {

    private final WallpaperManager mWallpaperManager;

    /**
     *
     */
    private int mCountX;

    /**
     *
     */
    private int mCountY;

    /**
     *
     */
    private int mWidthGap;
    /**
     *
     */
    private int mHeightGap;

    private int mCellWidth;
    private int mCellHeight;

    // --------------------------------------------------
    private boolean isHotSeatLayout;

    public ShortcutAndWidgetContainer(Context context) {
        super(context);
        mWallpaperManager = WallpaperManager.getInstance(context);
    }

    /**
     * 获取在(x, y)上的子view
     *
     * @param x x位置
     * @param y y位置
     * @return View
     */
    public View getChildAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
//            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
//            if ((lp.cellX <= x) && (x < lp.cellX + lp.cellHSpan) &&
//                    (lp.cellY <= y) && (y < lp.cellY + lp.cellVSpan)) {
//                return child;
//            }
        }
        return null;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child);
            }
        }
    }

    public void measureChild(View child) {
        final DeviceProfile profile = LauncherAppState.getInstance(getContext()).getDeviceProfile();

        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) child.getLayoutParams();
        if (layoutParams.isFullscreen) {
//            lp.x = 0;
//            lp.y = 0;
//            lp.width = getMeasuredWidth();
//            lp.height = getMeasuredHeight();
        } else {
//            layoutParams.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, invertLayoutHorizontally(), mCountX);
//            if (child instanceof LauncherAppWidgetHostView) {
            // Widgets have their own padding, so skip
//            } else {
            // Otherwise, center the icon
            int cellContentHeight = getCellContentHeight();
            int cellPaddingY = (int) Math.max(0, ((layoutParams.height - cellContentHeight) / 2f));
            int cellPaddingX = (int) (profile.edgeMarginPx / 2f);
            child.setPadding(cellPaddingX, cellPaddingY, cellPaddingX, 0);
//            }
        }
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    int getCellContentHeight() {
        final DeviceProfile profile = LauncherAppState.getInstance(getContext()).getDeviceProfile();
        return Math.min(getMeasuredHeight(), isHotSeatLayout ? profile.hotSeatCellHeightPx : profile.cellHeightPx);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }


    /**
     * 设置Cell的尺寸
     *
     * @param cellWidth  cellWidth
     * @param cellHeight cellHeight
     * @param widthGap
     * @param heightGap
     * @param countX
     * @param countY
     */
    public void setCellDimensions(int cellWidth, int cellHeight, int widthGap, int heightGap, int countX, int countY) {
        mCellWidth = cellWidth;
        mCellHeight = cellHeight;
        mWidthGap = widthGap;
        mHeightGap = heightGap;
        mCountX = countX;
        mCountY = countY;
    }

    public void setIsHotSeatLayout(boolean isHotSeatLayout) {
        this.isHotSeatLayout = isHotSeatLayout;
    }

    // --------------------------------------------------
    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
//        final int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            final View view = getChildAt(i);
//            view.setDrawingCacheEnabled(enabled);
//            // Update the drawing caches
//            if (!view.isHardwareAccelerated() && enabled) {
//                view.buildDrawingCache(true);
//            }
//        }
    }


}
