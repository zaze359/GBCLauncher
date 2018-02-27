package com.zaze.launcher.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-22 - 14:32
 */
@Entity(tableName = "workspace_screen")
public class WorkspaceScreen {

    @PrimaryKey
    private long id;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
