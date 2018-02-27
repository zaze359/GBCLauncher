package com.zaze.launcher.data.source.remote;

import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:24
 */
public class FavoritesRemoteDataSource implements FavoritesDataSource {

    private static FavoritesRemoteDataSource INSTANCE;

    private FavoritesRemoteDataSource() {
    }

    public static FavoritesRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FavoritesRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void saveFavorites(Favorites favorites) {

    }

    @Override
    public Observable<ArrayList<Long>> loadDefaultFavoritesIfNecessary() {
        return null;
    }

    @Override
    public Observable<List<Favorites>> loadFavorites() {
        return Observable.create(new ObservableOnSubscribe<List<Favorites>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Favorites>> e) throws Exception {
                List<Favorites> list = new ArrayList<>();
                list.add(new Favorites());
                e.onNext(list);
                e.onComplete();
            }
        }).map(new Function<List<Favorites>, List<Favorites>>() {
            @Override
            public List<Favorites> apply(List<Favorites> favorites) throws Exception {
                Thread.sleep(2000L);
                return favorites;
            }
        });
    }
}
