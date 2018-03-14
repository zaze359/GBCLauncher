package com.zaze.launcher;

import android.provider.BaseColumns;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-19 - 00:02
 */
public class LauncherSettings {
    /**
     * Columns required on table staht will be subject to backup and restore.
     */
    interface ChangeLogColumns extends BaseColumns {
        /**
         * The time of the last update to this row.
         * <P>Type: INTEGER</P>
         */
        String MODIFIED = "modified";
    }

    interface BaseLauncherColumns extends ChangeLogColumns {
        /**
         * Descriptive name of the gesture that can be displayed to the user.
         * <P>Type: TEXT</P>
         */
        String TITLE = "title";

        /**
         * The Intent URL of the gesture, describing what it points to. This
         * value is given to {@link android.content.Intent#parseUri(String, int)} to create
         * an Intent that can be launched.
         * <P>Type: TEXT</P>
         */
        String INTENT = "intent";

        /**
         * The type of the gesture
         * <p>
         * <P>Type: INTEGER</P>
         */
        String ITEM_TYPE = "itemType";

        /**
         * The gesture is an application
         */
        int ITEM_TYPE_APPLICATION = 0;

        /**
         * The gesture is an application created shortcut
         */
        int ITEM_TYPE_SHORTCUT = 1;

        /**
         * The icon type.
         * <P>Type: INTEGER</P>
         */
        String ICON_TYPE = "iconType";

        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        int ICON_TYPE_RESOURCE = 0;

        /**
         * The icon is a bitmap.
         */
        int ICON_TYPE_BITMAP = 1;

        /**
         * The icon package name, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        String ICON_PACKAGE = "iconPackage";

        /**
         * The icon resource id, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        String ICON_RESOURCE = "iconResource";

        /**
         * The custom icon bitmap, if icon type is ICON_TYPE_BITMAP.
         * <P>Type: BLOB</P>
         */
        String ICON = "icon";
    }

    public static class ItemColumns implements BaseLauncherColumns {

        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        public static final int CONTAINER_DESKTOP = -100;
        public static final int CONTAINER_HOT_SEAT = -101;

        public static String containerToString(int container) {
            switch (container) {
                case CONTAINER_DESKTOP:
                    return "desktop";
                case CONTAINER_HOT_SEAT:
                    return "hotseat";
                default:
                    return String.valueOf(container);
            }
        }

        /**
         * The favorite is a user created folder
         */
        public static final int ITEM_TYPE_FOLDER = 2;

        /**
         * 在Launcher3 中已被Google废弃
         */
        @Deprecated
        static final int ITEM_TYPE_LIVE_FOLDER = 3;

        /**
         * The favorite is a widget
         */
        public static final int ITEM_TYPE_APPWIDGET = 4;

        /**
         * The favorite is a custom widget provided by the launcher
         */
        public static final int ITEM_TYPE_CUSTOM_APPWIDGET = 5;

        /**
         * The favorite is a clock
         */
        @Deprecated
        static final int ITEM_TYPE_WIDGET_CLOCK = 1000;

        /**
         * The favorite is a search widget
         */
        @Deprecated
        static final int ITEM_TYPE_WIDGET_SEARCH = 1001;

        /**
         * The favorite is a photo frame
         */
        @Deprecated
        static final int ITEM_TYPE_WIDGET_PHOTO_FRAME = 1002;
    }

}
