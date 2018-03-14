package com.zaze.launcher.data.entity;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-28 - 17:25
 */
public class ItemInfo {
    /**
     * Intent extra to store the profile. Format: UserHandle
     */
    public static final String EXTRA_PROFILE = "profile";

    public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_FOLDER}, or
     * {@link LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
     */
    public int itemType;

    /**
     * The id of the container that holds this item. For the desktop, this will be
     * {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder.
     */
    public long container = NO_ID;

    /**
     * Iindicates the screen in which the shortcut appears.
     */
    public long screenId = -1;

    /**
     * Indicates the X position of the associated cell.
     */
    public int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    public int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    public int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    public int spanY = 1;

    /**
     * Indicates the minimum X cell span.
     */
    public int minSpanX = 1;

    /**
     * Indicates the minimum Y cell span.
     */
    public int minSpanY = 1;

    /**
     * Indicates the position in an ordered list.
     */
    public int rank = 0;

    /**
     * Title of the item
     */
    public String title;

    /**
     * Content description of the item.
     */
    public String contentDescription;


    public ItemInfo setValues(Favorites favorites) {
        this.id = favorites.getId();
        this.container = favorites.getContainer();
        this.screenId = favorites.getScreen();
        this.cellX = favorites.getCellX();
        this.cellY = favorites.getCellY();
        this.spanX = 1;
        this.spanY = 1;
        return this;
    }

}
