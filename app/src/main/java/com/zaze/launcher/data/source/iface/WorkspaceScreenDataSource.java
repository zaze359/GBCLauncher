package com.zaze.launcher.data.source.iface;

import com.zaze.launcher.data.entity.WorkspaceScreen;

import java.util.List;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-22 - 14:26
 */
public interface WorkspaceScreenDataSource {

    /**
     * 工作区页面
     */
    void saveWorkspaceScreen(List<Long> screenIds);

    /**
     * 查询所有workspaceScreen并返回。
     *
     * @return 所有workspaceScreen
     */
    List<WorkspaceScreen> loadWorkspaceScreens();
}
