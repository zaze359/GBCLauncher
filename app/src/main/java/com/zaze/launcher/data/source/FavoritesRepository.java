package com.zaze.launcher.data.source;

import android.content.Context;

import com.zaze.launcher.data.LauncherDatabase;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;
import com.zaze.launcher.data.source.local.FavoritesLocalDataSource;
import com.zaze.launcher.data.source.remote.FavoritesRemoteDataSource;

import java.util.List;

import io.reactivex.Observer;

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

    private FavoritesRepository(Context context) {
        LauncherDatabase database = LauncherDatabase.getInstance(context);
        this.localDataSource = FavoritesLocalDataSource.getInstance(database.favritesDao());
        this.remoteDataSource = FavoritesRemoteDataSource.getInstance();
    }

    @Override
    public void saveFavorites(Observer<Boolean> observer, Favorites... favorites) {
        localDataSource.saveFavorites(observer, favorites);
    }

    @Override
    public void loadDefaultFavoritesIfNecessary(Observer<List<Favorites>> observer) {
        localDataSource.loadFavorites(observer);
    }

    @Override
    public void loadFavorites(Observer<List<Favorites>> observer) {
        localDataSource.loadFavorites(observer);
        remoteDataSource.loadFavorites(observer);
    }
}
