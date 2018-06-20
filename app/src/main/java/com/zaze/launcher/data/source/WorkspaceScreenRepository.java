package com.zaze.launcher.data.source;

import android.content.Context;

import com.zaze.launcher.data.LauncherDatabase;
import com.zaze.launcher.data.entity.WorkspaceScreen;
import com.zaze.launcher.data.source.iface.WorkspaceScreenDataSource;
import com.zaze.launcher.data.source.local.WorkspaceScreenLocalDataSource;
import com.zaze.utils.log.ZLog;
import com.zaze.utils.log.ZTag;

import java.util.List;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-05-04 - 15:53
 */
public class WorkspaceScreenRepository implements WorkspaceScreenDataSource {
    private static WorkspaceScreenRepository INSTANCE = null;

    private WorkspaceScreenDataSource localDataSource;

    public static WorkspaceScreenRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WorkspaceScreenRepository(context);
        }
        return INSTANCE;
    }

    private WorkspaceScreenRepository(Context context) {
        localDataSource = new WorkspaceScreenLocalDataSource(LauncherDatabase.getInstance(context).workspaceScreenDao());
    }

    @Override
    public void saveWorkspaceScreen(List<Long> screenIds) {
        localDataSource.saveWorkspaceScreen(screenIds);
    }

    @Override
    public List<WorkspaceScreen> loadWorkspaceScreens() {
        ZLog.i(ZTag.TAG_DEBUG, "从数据库中loadWorkspaceScreens");
        return localDataSource.loadWorkspaceScreens();
    }
}
