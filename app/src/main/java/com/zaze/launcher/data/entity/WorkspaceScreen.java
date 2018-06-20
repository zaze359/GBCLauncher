package com.zaze.launcher.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Description : 工作区的分屏
 *
 * @author : ZAZE
 * @version : 2018-02-22 - 14:32
 */
@Entity(tableName = "workspace_screen")
public class WorkspaceScreen {

    @PrimaryKey
    private long id;

    private long screenRank;

    public WorkspaceScreen() {
    }

    @Ignore
    public WorkspaceScreen(long id, long screenRank) {
        this.id = id;
        this.screenRank = screenRank;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getScreenRank() {
        return screenRank;
    }

    public void setScreenRank(long screenRank) {
        this.screenRank = screenRank;
    }
}
