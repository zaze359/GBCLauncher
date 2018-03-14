package com.zaze.launcher.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.zaze.launcher.data.entity.ItemInfo;

import java.util.Comparator;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-02 - 14:08
 */
public class Folder extends LinearLayout {
    public Folder(Context context) {
        super(context);
    }

    public Folder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Folder(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Compares item position based on rank and position giving priority to the rank.
     * 比对item 根据排行和位置 的优先级进行排序
     */
    public static final Comparator<ItemInfo> ITEM_POS_COMPARATOR = new Comparator<ItemInfo>() {

        @Override
        public int compare(ItemInfo lhs, ItemInfo rhs) {
            if (lhs.rank != rhs.rank) {
                return lhs.rank - rhs.rank;
            } else if (lhs.cellY != rhs.cellY) {
                return lhs.cellY - rhs.cellY;
            } else {
                return lhs.cellX - rhs.cellX;
            }
        }
    };
}
