package com.zaze.launcher.util.parser;

import android.content.Context;
import android.content.Intent;

import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.entity.ShortcutInfo;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-06-06 - 23:21
 */
public class CommonAppTypeParser implements LayoutParserCallback<Favorites> {

    private static final int RESTORE_FLAG_BIT_SHIFT = 4;

    private final long mItemId;
    private final int mResId;
    private final Context mContext;


    public Favorites parsedValues;
    public Intent parsedIntent;
    public String parsedTitle;

    public CommonAppTypeParser(long itemId, int itemType, Context context) {
        mItemId = itemId;
        mContext = context;
        mResId = getResourceForItemType(itemType);
    }


    @Override
    public long insertAndCheck(Favorites values) {
        parsedValues = values;
        return 1;
    }


    /**
     * Tries to find a suitable app to the provided app type.
     * 尝试查找一个合适的app提供app type
     */
    public boolean findDefaultApp() {
        if (mResId == 0) {
            return false;
        }

        parsedIntent = null;
        parsedValues = null;
        new MyLayoutParser().loadLayout();
        return (parsedValues != null) && (parsedIntent != null);
    }


    public static int getResourceForItemType(int type) {
        switch (type) {
            // workaround for backup agent
            // 备份代理的解决方案
            default:
                return 0;
        }
    }


    /**
     * 一个特殊的解码算法
     * 得到itemType
     *
     * @param flag promiseType
     * @return ItemType
     */
    public static int decodeItemTypeFromFlag(int flag) {
        return (flag & ShortcutInfo.FLAG_RESTORED_APP_TYPE) >> RESTORE_FLAG_BIT_SHIFT;
    }

    private class MyLayoutParser extends DefaultLayoutParser {

        public MyLayoutParser() {
            super(CommonAppTypeParser.this.mContext, null, CommonAppTypeParser.this,
                    CommonAppTypeParser.this.mContext.getResources(), mResId, TAG_RESOLVE, 0);
        }

        @Override
        protected long addShortcut(String title, Intent intent, int type, Favorites values) {
            if (type == LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION) {
                parsedIntent = intent;
                parsedTitle = title;
            }
            return super.addShortcut(title, intent, type, values);
        }

    }


}
