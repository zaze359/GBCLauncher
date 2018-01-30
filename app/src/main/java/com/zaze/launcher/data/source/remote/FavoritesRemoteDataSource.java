package com.zaze.launcher.data.source.remote;

import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
    public void saveFavorites(Observer<Boolean> observer, Favorites... favorites) {
        //
    }

    @Override
    public void loadDefaultFavoritesIfNecessary(Observer<List<Favorites>> observer) {
        //
    }

    @Override
    public void loadFavorites(Observer<List<Favorites>> observer) {
        Observable.create(new ObservableOnSubscribe<List<Favorites>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Favorites>> e) throws Exception {
                List<Favorites> list = new ArrayList<>();
                list.add(new Favorites());
                e.onNext(list);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .map(new Function<List<Favorites>, List<Favorites>>() {
                    @Override
                    public List<Favorites> apply(List<Favorites> favorites) throws Exception {
                        Thread.sleep(2000L);
                        return favorites;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
