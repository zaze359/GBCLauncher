package com.zaze.launcher.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-16 - 14:09
 */
@Entity(tableName = "shortcut")
public class ShortcutInfo {

    @PrimaryKey
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
