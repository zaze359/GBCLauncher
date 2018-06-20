package com.zaze.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-07 - 16:21
 */
public abstract class PagedView extends ViewGroup {
    public static final int INVALID_RESTORE_PAGE = -1001;

    protected static final int INVALID_PAGE = -1;

    protected int mNextPage = INVALID_PAGE;
    protected int mCurrentPage;


    public PagedView(Context context) {
        super(context);
    }

    public PagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PagedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    /**
     * Returns the index of page to be shown immediately afterwards.
     * 返回随后要显示的页面索引。
     */
    public int getNextPage() {
        return (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
    }
}
