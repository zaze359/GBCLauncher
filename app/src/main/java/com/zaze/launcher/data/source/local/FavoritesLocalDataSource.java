package com.zaze.launcher.data.source.local;

import android.support.annotation.NonNull;

import com.zaze.launcher.data.dao.FavoritesDao;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;
import com.zaze.launcher.util.LogTag;
import com.zaze.utils.JsonUtil;
import com.zaze.utils.log.ZLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Description : 收藏数据(本地来源)
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:24
 */
public class FavoritesLocalDataSource implements FavoritesDataSource {

    private FavoritesDao favoritesDao;


    public FavoritesLocalDataSource(@NonNull FavoritesDao favoritesDao) {
        this.favoritesDao = checkNotNull(favoritesDao);
    }

    @Override
    public void insertOrReplaceFavorites(Favorites favorites) {
        ZLog.d(LogTag.TAG_DEBUG, "insertOrReplaceFavorites : " + JsonUtil.objToJson(favorites));
        favoritesDao.insertOrReplaceFavorites(favorites);
    }

    @Override
    public void deleteFavorites(List<Long> ids) {
        favoritesDao.deleteFavorites(ids);

    }

    @Override
    public List<Favorites> loadFavorites() {
        return favoritesDao.loadFavorites();
    }

    @Override
    public Long[] deleteEmptyFolders() {
        Long[] ids = favoritesDao.loadEmptyFolders();
        favoritesDao.deleteFavorites(Arrays.asList(ids));
        return ids;
    }

    @Override
    public void restoredRows(ArrayList<Long> restoredRows) {
        favoritesDao.restoredRows(restoredRows);
    }
}
