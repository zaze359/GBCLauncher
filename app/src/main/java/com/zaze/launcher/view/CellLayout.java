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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import com.zaze.launcher.DeviceProfile;
import com.zaze.launcher.LauncherAppState;

import java.util.Stack;

/**
 * 修改自 Launcher3 CellLayout(单元格布局？细胞布局？蜂巢布局？)
 *
 * @author zaze
 */
public class CellLayout extends ViewGroup {

    /**
     * X轴个数
     */
    int mCountX;
    /**
     * Y轴个数
     */
    int mCountY;
    // --------------------------------------------------
    /**
     * Cell的宽度
     * mFixedCellWidth < 0时使用
     */
    int mCellWidth;

    /**
     * Cell的高度
     * mFixedCellHeight < 0时使用
     */
    int mCellHeight;

    /**
     * Cell的固定宽度, 默认-1
     */
    private int mFixedCellWidth;
    /**
     * Cell的固定高度, 默认-1
     */
    private int mFixedCellHeight;

    /**
     * 每个Cell之间间隙的宽度
     * mOriginalWidthGap < 0时使用
     */
    int mWidthGap;
    /**
     * 每个Cell之间间隙的高度
     * mOriginalHeightGap < 0时使用
     */
    int mHeightGap;

    /**
     * 原始间隙宽度, 默认 = 0
     */
    private int mOriginalWidthGap;
    /**
     * 原始间隙高度, 默认 = 0
     */
    private int mOriginalHeightGap;

    /**
     * 间隙最大值, 默认 Integer.MAX_VALUE
     */
    private int mMaxGap;

    // These values allow a fixed measurement to be set on the CellLayout.

    private int mFixedWidth = -1;
    private int mFixedHeight = -1;
    // --------------------------------------------------
    /**
     * 记录单元格占用情况的数组
     */
    boolean[][] mOccupied;
    boolean[][] mTmpOccupied;

    private boolean isHotSeat;
    private float mHotSeatScale = 1f;

    private final Stack<Rect> mTempRectStack = new Stack<>();


    private ShortcutAndWidgetContainer mShortcutsAndWidgets;


    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setClipToPadding(false);
        //
        DeviceProfile grid = LauncherAppState.getInstance(context).getDeviceProfile();
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);
        mCellWidth = mCellHeight = -1;
        mFixedCellWidth = mFixedCellHeight = -1;
        mWidthGap = mOriginalWidthGap = 0;
        mHeightGap = mOriginalHeightGap = 0;
//        mMaxGap = Integer.MAX_VALUE;
        mCountX = grid.inv.numColumns;
        mCountY = grid.inv.numRows;

        mOccupied = new boolean[mCountX][mCountY];
        mTmpOccupied = new boolean[mCountX][mCountY];
//        mPreviousReorderDirection[0] = INVALID_DIRECTION;
//        mPreviousReorderDirection[1] = INVALID_DIRECTION;
        //
        setAlwaysDrawnWithCacheEnabled(false);
        mHotSeatScale = (float) grid.hotSeatIconSizePx / grid.iconSizePx;
        mShortcutsAndWidgets = new ShortcutAndWidgetContainer(context);
        mShortcutsAndWidgets.setCellDimensions(mCellWidth, mCellHeight, mWidthGap, mHeightGap, mCountX, mCountY);
//        mStylusEventHelper = new StylusEventHelper(this);
//
//        mTouchFeedbackView = new ClickShadowView(context);
//        addView(mTouchFeedbackView);
        addView(mShortcutsAndWidgets);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 计算子view的尺寸
        int childWidthSize = widthSize - getPaddingLeft() - getPaddingRight();
        int childHeightSize = heightSize - getPaddingTop() - getPaddingBottom();
        // --------------------------------------------------
        // 当固定的宽高<0时, 计算Cell的宽高
        if (mFixedCellWidth < 0 || mFixedCellHeight < 0) {
            int cw = DeviceProfile.calculateCellWidth(childWidthSize, mCountX);
            int ch = DeviceProfile.calculateCellHeight(childHeightSize, mCountY);
            // 发生变化则重置Cell的尺寸
            if (cw != mCellWidth || ch != mCellHeight) {
                mCellWidth = cw;
                mCellHeight = ch;
                mShortcutsAndWidgets.setCellDimensions(mCellWidth, mCellHeight, mWidthGap,
                        mHeightGap, mCountX, mCountY);
            }
        }

        // --------------------------------------------------
        // 当固定的间隙<0时, 计算
        if (mOriginalWidthGap < 0 || mOriginalHeightGap < 0) {
            // 计算间隙个数
            int numWidthGaps = mCountX - 1;
            int numHeightGaps = mCountY - 1;
            // 计算总的空闲空间
            int hFreeSpace = childWidthSize - (mCountX * mCellWidth);
            int vFreeSpace = childHeightSize - (mCountY * mCellHeight);
            // 重新计算间隙，并且重置Cell的尺寸
            mWidthGap = Math.min(mMaxGap, numWidthGaps > 0 ? (hFreeSpace / numWidthGaps) : 0);
            mHeightGap = Math.min(mMaxGap, numHeightGaps > 0 ? (vFreeSpace / numHeightGaps) : 0);
            mShortcutsAndWidgets.setCellDimensions(mCellWidth, mCellHeight, mWidthGap,
                    mHeightGap, mCountX, mCountY);
        } else {
            mWidthGap = mOriginalWidthGap;
            mHeightGap = mOriginalHeightGap;
        }
        // --------------------------------------------------
        // Make the feedback view large enough to hold the blur bitmap.
//        mTouchFeedbackView.measure(
//                MeasureSpec.makeMeasureSpec(mCellWidth + mTouchFeedbackView.getExtraSize(),
//                        MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(mCellHeight + mTouchFeedbackView.getExtraSize(),
//                        MeasureSpec.EXACTLY));

        // --------------------------------------------------
        int newWidth = childWidthSize;
        int newHeight = childHeightSize;
        boolean isUseFixedSize = mFixedWidth > 0 && mFixedHeight > 0;
        if (isUseFixedSize) {
            newWidth = mFixedWidth;
            newHeight = mFixedHeight;
        } else if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }
        mShortcutsAndWidgets.measure(
                MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
        // --------------------------------------------------
        if (isUseFixedSize) {
            // 若是固定尺寸则使用新尺寸
            setMeasuredDimension(newWidth, newHeight);
        } else {
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    public void setIsHotSeat(boolean isHotSeat) {
        this.isHotSeat = isHotSeat;
        mShortcutsAndWidgets.setIsHotSeatLayout(isHotSeat);
    }


    /**
     * 设置表格尺寸
     *
     * @param xCount x轴个数
     * @param yCount y轴个数
     */
    public void setGridSize(int xCount, int yCount) {
        mCountX = xCount;
        mCountY = yCount;
        mOccupied = new boolean[mCountX][mCountY];
        mTmpOccupied = new boolean[mCountX][mCountY];
        mTempRectStack.clear();
        mShortcutsAndWidgets.setCellDimensions(mCellWidth, mCellHeight, mWidthGap, mHeightGap,
                mCountX, mCountY);
        requestLayout();
    }


    public boolean addViewToCellLayout(View child, int index, int childId, LayoutParams params, boolean markCells) {
        final LayoutParams lp = params;
        // HotSeat icons - remove text
//        if (child instanceof BubbleTextView) {
//            BubbleTextView bubbleChild = (BubbleTextView) child;
//            bubbleChild.setTextVisibility(!mIsHotseat);
//        }
        child.setScaleX(getChildrenScale());
        child.setScaleY(getChildrenScale());
        // Generate an id for each view, this assumes we have at most 256x256 cells
        // per workspace screen
        if (lp.cellX >= 0 && lp.cellX <= mCountX - 1 && lp.cellY >= 0 && lp.cellY <= mCountY - 1) {
            // 在CellLayout 内部
            if (lp.cellHSpan < 0) {
                lp.cellHSpan = mCountX;
            }
            if (lp.cellVSpan < 0) {
                lp.cellVSpan = mCountY;
            }
            child.setId(childId);
            mShortcutsAndWidgets.addView(child, index, lp);
            if (markCells) {
                markCellsAsOccupiedForView(child);
            }
            return true;
        }
        return false;
    }

    /**
     * 标记占用
     *
     * @param view
     */
    public void markCellsAsOccupiedForView(View view) {
        if (view == null || view.getParent() != mShortcutsAndWidgets) {
            return;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        markCellsForView(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, mOccupied, true);
    }

    /**
     * 标记
     *
     * @param cellX    cellX
     * @param cellY    cellY
     * @param spanX    spanX
     * @param spanY    spanY
     * @param occupied occupied占用数组
     * @param value    是否占用
     */
    private void markCellsForView(int cellX, int cellY, int spanX, int spanY, boolean[][] occupied, boolean value) {
        if (cellX < 0 || cellY < 0) {
            return;
        }
        for (int x = cellX; x < cellX + spanX && x < mCountX; x++) {
            for (int y = cellY; y < cellY + spanY && y < mCountY; y++) {
                occupied[x][y] = value;
            }
        }
    }

    /**
     * 缩放
     *
     * @return float
     */
    public float getChildrenScale() {
        return isHotSeat ? mHotSeatScale : 1.0f;
    }

    public int getCountX() {
        return mCountX;
    }

    public int getCountY() {
        return mCountY;
    }


    // --------------------------------------------------
    // --------------------------------------------------

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        /**
         * Indicates that this item should use the full extents of its parent.
         */
        public boolean isFullscreen = false;

        /**
         * Horizontal location of the item in the grid.
         * 在网格中的X轴位置
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         * 在网格中的Y轴位置
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Temporary horizontal location of the item in the grid during reorder
         * 重新排列时 在网格中的临时X轴位置
         */
        public int tmpCellX;

        /**
         * Temporary vertical location of the item in the grid during reorder
         * 重新排列时 在网格中的临时Y轴位置
         */
        public int tmpCellY;

        /**
         * Number of cells spanned horizontally by the item.
         * 跨度，占多少个单位？
         */
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         * 跨度，占多少个单位？
         */
        public int cellVSpan;

        /**
         * Indicates whether this item can be reordered. Always true except in the case of the
         * the AllApps button.
         * 是否需要重新排序， 除了AllApps button,其他的总是需要默认排序
         */
        public boolean canReorder = true;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.cellX = source.cellX;
            this.cellY = source.cellY;
            this.cellHSpan = source.cellHSpan;
            this.cellVSpan = source.cellVSpan;
        }

        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

    }

    public static final class CellInfo {
        View cell;
        int cellX = -1;
        int cellY = -1;
        int spanX;
        int spanY;
        long screenId;
        long container;
    }
}
