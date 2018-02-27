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
     */
    private long container;

    /**
     * The screen holding the favorite (if container is CONTAINER_DESKTOP)
     */
    private long screen;

    /**
     * The X span of the cell holding the favorite
     */
    private int spanX;

    /**
     * The Y span of the cell holding the favorite
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
}
