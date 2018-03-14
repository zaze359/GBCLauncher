package com.zaze.launcher.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-09 - 20:08
 */
public class Workspace extends PagedView {
    protected int mCurrentPage;

    public static final int SCREEN_COUNT = 5;

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
}
