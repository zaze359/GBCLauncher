package com.zaze.launcher.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.zaze.launcher.data.entity.Favorites;

import java.util.List;

/**
 * Description : 收藏
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:23
 */
@Dao
public interface FavoritesDao {


    /**
     * 不存在插入，存在则替换
     *
     * @param favorites
     * @return
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorites(Favorites favorites);

    /**
     * 查询所有收藏
     *
     * @return 所有收藏
     */
    @Query("SELECT * FROM favorites")
    List<Favorites> loadFavorites();

    /**
     * 加载空目录
     *
     * @return 空目录
     */
    @Query("SELECT id FROM favorites WHERE itemType = 2 AND id NOT IN (SELECT container FROM favorites)")
    Long[] loadEmptyFolders();

    /**
     * 删除收藏
     *
     * @param ids ids
     */
    @Query("DELETE FROM favorites WHERE id IN (:ids)")
    void deleteFavorites(List<Long> ids);


    /**
     * 恢复rows
     *
     * @param restoredRows ids
     */
    @Query("UPDATE favorites SET restored = 0 WHERE id IN (:restoredRows)")
    void restoredRows(List<Long> restoredRows);
}
