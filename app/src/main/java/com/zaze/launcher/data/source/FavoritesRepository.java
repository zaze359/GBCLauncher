package com.zaze.launcher.data.source;

import android.content.Context;

import com.zaze.launcher.data.LauncherDatabase;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;
import com.zaze.launcher.data.source.local.FavoritesLocalDataSource;
import com.zaze.launcher.data.source.remote.FavoritesRemoteDataSource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:28
 */
public class FavoritesRepository implements FavoritesDataSource {
    private static FavoritesRepository INSTANCE = null;

    private final FavoritesDataSource localDataSource;
    private final FavoritesDataSource remoteDataSource;

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
        this.localDataSource = FavoritesLocalDataSource.getInstance(LauncherDatabase.getInstance(context).favritesDao());
        this.remoteDataSource = FavoritesRemoteDataSource.getInstance();
    }

    @Override
    public void saveFavorites(Favorites favorites) {
        localDataSource.saveFavorites(favorites);
    }

    @Override
    public void deleteFavorites(List<Long> ids) {
        localDataSource.deleteFavorites(ids);
    }

    @Override
    public Observable<ArrayList<Long>> loadDefaultFavoritesIfNecessary() {
        return localDataSource.loadDefaultFavoritesIfNecessary();
    }

    @Override
    public Observable<List<Favorites>> loadFavorites() {
        return localDataSource.loadFavorites()
                .map(new Function<List<Favorites>, List<Favorites>>() {
                    @Override
                    public List<Favorites> apply(List<Favorites> favorites) throws Exception {
                        return favorites;
                    }
                });
//        remoteDataSource.loadFavorites(observer);
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
