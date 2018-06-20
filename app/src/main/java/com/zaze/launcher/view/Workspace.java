package com.zaze.launcher.view;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.zaze.launcher.LauncherActivity;
import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.data.entity.ItemInfo;
import com.zaze.launcher.util.FocusHelper;
import com.zaze.launcher.util.LogTag;
import com.zaze.launcher.util.LongArrayMap;
import com.zaze.utils.log.ZLog;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-09 - 20:08
 */
public class Workspace extends PagedView {
    private static final String TAG = "Workspace" + LogTag.TAG_BASE;

    protected int mCurrentPage;
    public static final int SCREEN_COUNT = 5;
    private LauncherActivity mLauncher;
    /**
     * The screen id used for the empty screen always present to the right.
     * 表示空屏幕的screenId,
     */
    final static long EXTRA_EMPTY_SCREEN_ID = -201;

    protected static final int PAGE_SNAP_ANIMATION_DURATION = 750;

    protected boolean mForceScreenScrolled = false;

    protected OnLongClickListener mLongClickListener;

    //    protected LauncherScroller mScroller;
    private boolean mFreeScroll = false;
    private int[] mPageScrolls;

    protected int[] mTempVisiblePagesRange = new int[2];

    LongArrayMap<CellLayout> mWorkspaceScreens = new LongArrayMap<>();


    // --------------------------------------------------

    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Workspace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = (LauncherActivity) context;
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getPageCount();
        for (int i = 0; i < count; i++) {
            getPageAt(i).setOnLongClickListener(l);
        }
        super.setOnLongClickListener(l);
    }

    // --------------------------------------------------

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void clearDropTargets() {
        ZLog.i(TAG, "clearDropTargets");
    }

    public void removeAllWorkspaceScreens() {
        ZLog.i(TAG, "removeAllWorkspaceScreens");
    }


    public void insertNewWorkspaceScreenBeforeEmptyScreen(long screenId) {
        ZLog.i(TAG, "insertNewWorkspaceScreenBeforeEmptyScreen : " + screenId);
    }

    /**
     * 添加一个空白页
     */
    public void addExtraEmptyScreen() {
        ZLog.i(TAG, "addExtraEmptyScreen");
    }

    public void createCustomContentContainer() {
    }

    /**
     * 在绑定时，我们使用rank (screenId)来计算hotseat项目的x和y。有关参数定义，请参见实现。
     *
     * @param child
     * @param container
     * @param screenId
     * @param x
     * @param y
     * @param spanX
     * @param spanY
     */
    public void addInScreenFromBind(View child, long container, long screenId, int x, int y,
                                    int spanX, int spanY) {
        addInScreen(child, container, screenId, x, y, spanX, spanY, false, true);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     * <p>
     * 在指定的屏幕中添加指定的子元素。子节点的位置和维度由x、y、spanX和spanY定义
     *
     * @param child             The child to add in one of the workspace's screens.
     * @param screenId          The screen in which to add the child.
     * @param x                 The X position of the child in the screen's grid.
     * @param y                 The Y position of the child in the screen's grid.
     * @param spanX             The number of cells spanned horizontally by the child.
     * @param spanY             The number of cells spanned vertically by the child.
     * @param insert            When true, the child is inserted at the beginning of the children list.
     * @param computeXYFromRank When true, we use the rank (stored in screenId) to compute
     *                          the x and y position in which to place hotseat items. Otherwise
     *                          we use the x and y position to compute the rank.
     */
    void addInScreen(View child, long container, long screenId, int x, int y, int spanX, int spanY,
                     boolean insert, boolean computeXYFromRank) {
        if (container == LauncherSettings.ItemColumns.CONTAINER_DESKTOP) {
            // 是桌面
            if (getScreenWithId(screenId) == null) {
                ZLog.e(TAG, "Skipping child, screenId " + screenId + " not found");
                // DEBUGGING - Print out the stack trace to see where we are adding from
                new Throwable().printStackTrace();
                return;
            }
        }
        if (screenId == EXTRA_EMPTY_SCREEN_ID) {
            // This should never happen
            throw new RuntimeException("Screen id should not be EXTRA_EMPTY_SCREEN_ID");
        }

        final CellLayout layout;
        if (container == LauncherSettings.ItemColumns.CONTAINER_HOT_SEAT) {
            layout = mLauncher.getHotSeat().getLayout();
            child.setOnKeyListener(new FocusHelper.HotSeatIconKeyEventListener());
            // Hide folder title in the hotseat
            if (child instanceof FolderIcon) {
                ((FolderIcon) child).setTextVisible(false);
            }

            if (computeXYFromRank) {
                x = mLauncher.getHotSeat().getCellXFromOrder((int) screenId);
                y = mLauncher.getHotSeat().getCellYFromOrder((int) screenId);
            } else {
                screenId = mLauncher.getHotSeat().getOrderInHotSeat(x, y);
            }
        } else {
            // Show folder title if not in the hotseat
            if (child instanceof FolderIcon) {
                ((FolderIcon) child).setTextVisible(true);
            }
            layout = getScreenWithId(screenId);
            child.setOnKeyListener(new FocusHelper.IconKeyEventListener());
        }

        ViewGroup.LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }

        if (spanX < 0 && spanY < 0) {
            lp.isLockedToGrid = false;
        }

        // Get the canonical child id to uniquely represent this view in this screen
        ItemInfo info = (ItemInfo) child.getTag();
        int childId = mLauncher.getViewIdForItem(info);

        boolean markCellsAsOccupied = !(child instanceof Folder);
        if (!layout.addViewToCellLayout(child, insert ? 0 : -1, childId, lp, markCellsAsOccupied)) {
            ZLog.e(TAG, "Failed to add to item at (" + lp.cellX + "," + lp.cellY + ") to CellLayout", true);
        }
        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        // TODO
//        if (child instanceof DropTarget) {
//            mDragController.addDropTarget((DropTarget) child);
//        }
    }


    // --------------------------------------------------


    public CellLayout getScreenWithId(long screenId) {
        return mWorkspaceScreens.get(screenId);
    }


    int getPageCount() {
        return getChildCount();
    }

    public View getPageAt(int index) {
        return getChildAt(index);
    }

    protected void getFreeScrollPageRange(int[] range) {
        range[0] = 0;
        range[1] = Math.max(0, getChildCount() - 1);
    }

    public int getScrollForPage(int index) {
        if (mPageScrolls == null || index >= mPageScrolls.length || index < 0) {
            return 0;
        } else {
            return mPageScrolls[index];
        }
    }

    // --------------------------------------------------

    private void updatePageIndicator() {
        // TODO Update the page indicator (when we aren't reordering)
//        if (mPageIndicator != null) {
//            mPageIndicator.setContentDescription(getPageIndicatorDescription());
//            if (!isReordering(false)) {
//                mPageIndicator.setActiveMarker(getNextPage());
//            }
//        }
    }

    protected void pageBeginMoving() {
        // TODO
//        if (!mIsPageMoving) {
//            mIsPageMoving = true;
//            onPageBeginMoving();
//        }
    }

    // --------------------------------------------------

    private void abortScrollerAnimation(boolean resetNextPage) {
        // TODO
//        mScroller.abortAnimation();
        // We need to clean up the next page here to avoid computeScrollHelper from
        // updating current page on the pass.
        if (resetNextPage) {
            mNextPage = INVALID_PAGE;
        }
    }

    private int validateNewPage(int newPage) {
        int validatedPage = newPage;
        // When in free scroll mode, we need to clamp to the free scroll page range.
        // 当处于自由滚动模式时，我们需要固定自由滚动页面范围
        if (mFreeScroll) {
            // 0 ~ count-1
            getFreeScrollPageRange(mTempVisiblePagesRange);
            // > 0  newPage < count -1 ?  newPage : count-1
            validatedPage = Math.max(mTempVisiblePagesRange[0],
                    Math.min(newPage, mTempVisiblePagesRange[1]));
        }
        // Ensure that it is clamped by the actual set of children in all cases
        // > 0 newPage < count -1 ?  newPage : count-1
        validatedPage = Math.max(0, Math.min(validatedPage, getPageCount() - 1));
        return validatedPage;
    }

    // --------------------------------------------------

    public void snapToPage(int whichPage) {
        snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
    }

    protected void snapToPage(int whichPage, int duration) {
        snapToPage(whichPage, duration, false, null);
    }

    protected void snapToPage(int whichPage, int duration, boolean immediate,
                              TimeInterpolator interpolator) {
        whichPage = validateNewPage(whichPage);

        int newX = getScrollForPage(whichPage);
        final int delta = newX - getScrollX();
        snapToPage(whichPage, delta, duration, immediate, interpolator);
    }

    protected void snapToPage(int whichPage, int delta, int duration, boolean immediate,
                              TimeInterpolator interpolator) {
        whichPage = validateNewPage(whichPage);
        mNextPage = whichPage;
        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichPage != mCurrentPage &&
                focusedChild == getPageAt(mCurrentPage)) {
            focusedChild.clearFocus();
        }

        pageBeginMoving();
        awakenScrollBars(duration);
        if (immediate) {
            duration = 0;
        } else if (duration == 0) {
            duration = Math.abs(delta);
        }

        // TODO
//        if (!mScroller.isFinished()) {
//            abortScrollerAnimation(false);
//        }
//
//        if (interpolator != null) {
//            mScroller.setInterpolator(interpolator);
//        } else {
//            mScroller.setInterpolator(mDefaultInterpolator);
//        }
//        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
//
        updatePageIndicator();
        // Trigger a compute() to finish switching pages if necessary
        if (immediate) {
            computeScroll();
        }

        mForceScreenScrolled = true;
        invalidate();
    }


}
