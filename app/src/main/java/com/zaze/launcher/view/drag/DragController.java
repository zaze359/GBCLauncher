package com.zaze.launcher.view.drag;

import android.content.Context;

/**
 * Description :
 * Class for initiating a drag within a view or across multiple views.
 *
 * @author : ZAZE
 * @version : 2018-01-07 - 00:59
 */
public class DragController {

    private boolean mDragging = false;


    public DragController(Context context) {
    }

    public boolean isDragging() {
        return mDragging;
    }
}
