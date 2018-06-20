package com.zaze.launcher.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.zaze.launcher.data.entity.WorkspaceScreen;

import java.util.List;

/**
 * Description : 工作区分屏
 *
 * @author : ZAZE
 * @version : 2018-02-22 - 14:37
 */
@Dao
public interface WorkspaceScreenDao {

    /**
     * 插入更新工作区屏
     *
     * @param screen screen
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkspaceScreen(WorkspaceScreen screen);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkspaceScreens(List<WorkspaceScreen> screens);

    /**
     * 加载已保存的工作区屏
     *
     * @return List<WorkspaceScreen>
     */
    @Query("SELECT * FROM workspace_screen")
    List<WorkspaceScreen> loadWorkspaceScreens();


    /**
     * 清空
     */
    @Query("DELETE FROM workspace_screen")
    void clearWorkspaceScreen();

}
