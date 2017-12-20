package com.zaze.launcher.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2017-12-17 - 20:50
 */
public class DragLayer extends InsetTableFrameLayout {
    public DragLayer(@NonNull Context context) {
        super(context);
    }

    public DragLayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragLayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
