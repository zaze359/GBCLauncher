package com.zaze.launcher.data.source;

import android.content.Context;

import com.zaze.launcher.data.LauncherDatabase;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;
import com.zaze.launcher.data.source.local.FavoritesLocalDataSource;
import com.zaze.launcher.util.LogTag;
import com.zaze.utils.log.ZLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:28
 */
public class FavoritesRepository implements FavoritesDataSource {
    private static FavoritesRepository INSTANCE = null;

    private final FavoritesDataSource localDataSource;

    public static FavoritesRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new FavoritesRepository(context);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private FavoritesRepository(Context context) {
        this.localDataSource = new FavoritesLocalDataSource(LauncherDatabase.getInstance(context).favritesDao());
    }

    @Override
    public void insertOrReplaceFavorites(Favorites favorites) {
        localDataSource.insertOrReplaceFavorites(favorites);
    }

    @Override
    public void deleteFavorites(List<Long> ids) {
        localDataSource.deleteFavorites(ids);
    }

    @Override
    public List<Favorites> loadFavorites() {
        ZLog.i(LogTag.TAG_LOADER, "从数据库加载所有收藏");
        return localDataSource.loadFavorites();
    }

    @Override
    public Long[] deleteEmptyFolders() {
        return localDataSource.deleteEmptyFolders();
    }

    @Override
    public void restoredRows(ArrayList<Long> restoredRows) {
        localDataSource.restoredRows(restoredRows);
    }

}
