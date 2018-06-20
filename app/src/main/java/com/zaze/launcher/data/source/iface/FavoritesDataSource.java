package com.zaze.launcher.data.source.iface;

import com.zaze.launcher.data.entity.Favorites;

import java.util.ArrayList;
import java.util.List;

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
    void insertOrReplaceFavorites(Favorites favorites);

    /**
     * 删除收藏
     *
     * @param ids ids
     */
    void deleteFavorites(List<Long> ids);


    /**
     * 加载收藏夹
     *
     * @return 加载收藏夹
     */
    List<Favorites> loadFavorites();

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
