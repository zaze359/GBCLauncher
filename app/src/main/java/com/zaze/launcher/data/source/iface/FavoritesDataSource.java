package com.zaze.launcher.data.source.iface;

import com.zaze.launcher.data.entity.Favorites;

import java.util.List;

import io.reactivex.Observer;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:26
 */
public interface FavoritesDataSource {

    /**
     * 保存
     *
     * @param observer observer
     */
    @Deprecated
    void saveFavorites(Observer<Boolean> observer, Favorites... favorites);

    /**
     * 加载默认收藏
     *
     * @param observer
     */
    void loadDefaultFavoritesIfNecessary(Observer<List<Favorites>> observer);

    /**
     * 加载收藏夹
     */
    void loadFavorites(Observer<List<Favorites>> observer);

}
