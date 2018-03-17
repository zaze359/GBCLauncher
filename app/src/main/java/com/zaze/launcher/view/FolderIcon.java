package com.zaze.launcher.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zaze.launcher.data.entity.FolderInfo;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-28 - 14:38
 */
public class FolderIcon extends FrameLayout {

    /**
     * The number of icons to display in the
     */
    public static final int NUM_ITEMS_IN_PREVIEW = 3;

    public FolderIcon(@NonNull Context context) {
        super(context);
    }

    public FolderIcon(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderIcon(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public static FolderIcon fromXml(int resId, Context context, ViewGroup group,
                                     FolderInfo folderInfo, IconCache iconCache) {
        FolderIcon icon = (FolderIcon) LayoutInflater.from(context).inflate(resId, group, false);
        return icon;
    }

}
