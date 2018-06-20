package com.zaze.launcher.data.source.local;

import android.support.annotation.NonNull;

import com.zaze.launcher.data.dao.WorkspaceScreenDao;
import com.zaze.launcher.data.entity.WorkspaceScreen;
import com.zaze.launcher.data.source.iface.WorkspaceScreenDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-02-22 - 14:26
 */
public class WorkspaceScreenLocalDataSource implements WorkspaceScreenDataSource {

    private WorkspaceScreenDao workspaceScreenDao;

    public WorkspaceScreenLocalDataSource(@NonNull WorkspaceScreenDao workspaceScreenDao) {
        this.workspaceScreenDao = workspaceScreenDao;
    }

    @Override
    public void saveWorkspaceScreen(List<Long> screenIds) {
        List<WorkspaceScreen> list = new ArrayList<>();
        if (!screenIds.isEmpty()) {
            int rank = 0;
            for (long id : screenIds) {
                list.add(new WorkspaceScreen(id, rank));
                rank++;
            }
            workspaceScreenDao.insertWorkspaceScreens(list);
        }
    }

    @Override
    public List<WorkspaceScreen> loadWorkspaceScreens() {
        return workspaceScreenDao.loadWorkspaceScreens();
    }
}
