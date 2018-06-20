package com.zaze.launcher.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:23
 */
@Entity(tableName = "favorites")
public class Favorites {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;

    private String intent;

    private int itemType;

    /**
     * The container holding the favorite
     * 持有这个Favorite的容器id
     */
    private long container;

    /**
     * The screen holding the favorite (if container is CONTAINER_DESKTOP)
     */
    private long screen;

    /**
     * The X span of the cell holding the favorite
     * 这个favorite在单元格中的X跨度
     */
    private int spanX;

    /**
     * The Y span of the cell holding the favorite
     * 这个favorite在单元格中的Y跨度
     */
    private int spanY;

    /**
     * The X coordinate of the cell holding the favorite
     * (if container is CONTAINER_DESKTOP)
     */
    private int cellX;

    /**
     * The Y coordinate of the cell holding the favorite
     * (if container is CONTAINER_DESKTOP)
     */
    private int cellY;


    /**
     * The profile id of the item in the cell.
     * <p>
     * Type: INTEGER
     * </P>
     */
    private int profileId;


    /**
     * Boolean indicating that his item was restored and not yet successfully bound.
     */
    private int restored;

    /**
     * Indicates the position of the item inside an auto-arranged view like folder or hotseat.
     */
    private int rank;

    /**
     * The icon type.
     */
    private int iconType;

    /**
     * The icon package name, if icon type is ICON_TYPE_RESOURCE.
     */
    private String iconPackage;

    /**
     * The icon resource id, if icon type is ICON_TYPE_RESOURCE.
     */
    private String iconResource;

    /**
     * The custom icon bitmap, if icon type is ICON_TYPE_BITMAP.
     */
    private String icon;

    /**
     * Stores general flag based options for {@link ItemInfo}s.
     * <p>Type: INTEGER</p>
     */
    private int options;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getContainer() {
        return container;
    }

    public void setContainer(long container) {
        this.container = container;
    }

    public long getScreen() {
        return screen;
    }

    public void setScreen(long screen) {
        this.screen = screen;
    }

    public int getSpanX() {
        return spanX;
    }

    public void setSpanX(int spanX) {
        this.spanX = spanX;
    }

    public int getSpanY() {
        return spanY;
    }

    public void setSpanY(int spanY) {
        this.spanY = spanY;
    }

    public int getCellX() {
        return cellX;
    }

    public void setCellX(int cellX) {
        this.cellX = cellX;
    }

    public int getCellY() {
        return cellY;
    }

    public void setCellY(int cellY) {
        this.cellY = cellY;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getRestored() {
        return restored;
    }

    public void setRestored(int restored) {
        this.restored = restored;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getIconType() {
        return iconType;
    }

    public void setIconType(int iconType) {
        this.iconType = iconType;
    }

    public String getIconPackage() {
        return iconPackage;
    }

    public void setIconPackage(String iconPackage) {
        this.iconPackage = iconPackage;
    }

    public String getIconResource() {
        return iconResource;
    }

    public void setIconResource(String iconResource) {
        this.iconResource = iconResource;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getOptions() {
        return options;
    }

    public void setOptions(int options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "Favorites{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", intent='" + intent + '\'' +
                ", itemType=" + itemType +
                ", container=" + container +
                ", screen=" + screen +
                ", spanX=" + spanX +
                ", spanY=" + spanY +
                ", cellX=" + cellX +
                ", cellY=" + cellY +
                ", profileId=" + profileId +
                ", restored=" + restored +
                ", rank=" + rank +
                ", iconType=" + iconType +
                ", iconPackage='" + iconPackage + '\'' +
                ", iconResource='" + iconResource + '\'' +
                ", icon='" + icon + '\'' +
                ", options=" + options +
                '}';
    }
}
