package com.zaze.launcher.data;

import android.appwidget.AppWidgetHost;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.zaze.launcher.data.dao.FavoritesDao;
import com.zaze.launcher.data.dao.ShortcutDao;
import com.zaze.launcher.data.dao.WorkspaceScreenDao;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.entity.WorkspaceScreen;
import com.zaze.launcher.util.LauncherSharePref;
import com.zaze.launcher.util.LogTag;
import com.zaze.utils.log.ZLog;

/**
 * Description : 单例数据库类
 *
 * @author : ZAZE
 * @version : 2018-01-16 - 14:52
 */
@Database(entities = {Favorites.class, WorkspaceScreen.class}, version = 2)
public abstract class LauncherDatabase extends RoomDatabase {

    private static final String EMPTY_DATABASE_CREATED = "EMPTY_DATABASE_CREATED";
    private static final int APPWIDGET_HOST_ID = 1024;
    private static AppWidgetHost appWidgetHost;
    private static LauncherDatabase INSTANCE;

    private static final Object S_LOCK = new Object();

    public static LauncherDatabase getInstance(Context context) {
        synchronized (S_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        LauncherDatabase.class, "gbc_launcher.db")
                        .addMigrations(MIGRATION_1_2)
                        .build();
                appWidgetHost = new AppWidgetHost(context, APPWIDGET_HOST_ID);
            }
            return INSTANCE;
        }
    }

    /**
     * version 1 升级到version 2
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            ZLog.i(LogTag.TAG_DEBUG, "MIGRATION_1_2");
        }
    };

    // --------------------------------------------------

    public static boolean isEmptyDatabaseCreate() {
        return LauncherSharePref.get(EMPTY_DATABASE_CREATED, true);
    }

    public static void clearFlagEmptyDbCreated() {
        LauncherSharePref.commit(EMPTY_DATABASE_CREATED, false);
    }


    // --------------------------------------------------

    public static AppWidgetHost getAppWidgetHost() {
        return appWidgetHost;
    }

// --------------------------------------------------

    public abstract ShortcutDao shortcutDao();

    public abstract FavoritesDao favritesDao();

    public abstract WorkspaceScreenDao workspaceScreenDao();

}
