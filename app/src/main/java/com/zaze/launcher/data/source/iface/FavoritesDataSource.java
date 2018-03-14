package com.zaze.launcher.data.source.iface;

import com.zaze.launcher.data.entity.Favorites;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:26
 */
public interface FavoritesDataSource {

    /**
     * 保存收藏
     *
     * @param favorites favorites
     */
    void saveFavorites(Favorites favorites);

    /**
     * 删除收藏
     *
     * @param ids ids
     */
    void deleteFavorites(List<Long> ids);

    /**
     * 加载默认收藏配置
     * Loads the default workspace based on the following priority scheme:
     * 1) From the app restrictions
     * 2) From a package provided by play store
     * 3) From a partner configuration APK, already in the system image
     * 4) The default configuration for the particular device
     *
     * @return screenIds
     */
    Observable<ArrayList<Long>> loadDefaultFavoritesIfNecessary();

    /**
     * 加载收藏夹
     *
     * @return 加载收藏夹
     */
    Observable<List<Favorites>> loadFavorites();

    /**
     * 删除空目录
     *
     * @return 删除的空目录ids
     */
    Long[] deleteEmptyFolders();

    /**
     * 恢复rows
     *
     * @param restoredRows ids
     */
    void restoredRows(ArrayList<Long> restoredRows);
}
