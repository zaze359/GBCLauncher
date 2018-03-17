package com.zaze.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

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

    LongArrayMap<CellLayout> mWorkspaceScreens = new LongArrayMap<>();

    public Workspace(Context context) {
        super(context);
    }

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Workspace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

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

    // At bind time, we use the rank (screenId) to compute x and y for hotseat items.
    // See implementation for parameter definition.
    public void addInScreenFromBind(View child, long container, long screenId, int x, int y,
                                    int spanX, int spanY) {
//        addInScreen(child, container, screenId, x, y, spanX, spanY, false, true);
    }

    public CellLayout getScreenWithId(long screenId) {
        return mWorkspaceScreens.get(screenId);
    }
}
