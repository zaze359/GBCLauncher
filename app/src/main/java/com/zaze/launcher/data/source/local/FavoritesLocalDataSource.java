package com.zaze.launcher.data.source.local;

import android.support.annotation.NonNull;

import com.zaze.launcher.data.LauncherDatabase;
import com.zaze.launcher.data.dao.FavoritesDao;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;
import com.zaze.launcher.util.parser.AutoInstallsLayout;
import com.zaze.launcher.util.parser.LayoutParserCallback;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Description : 收藏数据(本地来源)
 *
 * @author : ZAZE
 * @version : 2018-01-20 - 13:24
 */
public class FavoritesLocalDataSource implements FavoritesDataSource {
    private static volatile FavoritesLocalDataSource INSTANCE;

    private FavoritesDao favoritesDao;

    public static FavoritesLocalDataSource getInstance(@NonNull FavoritesDao favoritesDao) {
        if (INSTANCE == null) {
            synchronized (FavoritesLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FavoritesLocalDataSource(favoritesDao);
                }
            }
        }
        return INSTANCE;
    }

    private FavoritesLocalDataSource(@NonNull FavoritesDao favoritesDao) {
        this.favoritesDao = checkNotNull(favoritesDao);
    }

    @Override
    public void saveFavorites(Observer<Boolean> observer, Favorites... favorites) {
        Observable.just(favorites)
                .subscribeOn(Schedulers.io())
                .map(new Function<Favorites[], Boolean>() {
                    @Override
                    public Boolean apply(Favorites[] favorites) throws Exception {
                        favoritesDao.insertFavorites(favorites);
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void loadDefaultFavoritesIfNecessary(Observer<List<Favorites>> observer) {

        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(LauncherDatabase.isEmptyDatabaseCreate());
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .map(new Function<Boolean, List<Favorites>>() {
                    @Override
                    public List<Favorites> apply(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            AutoInstallsLayout.createWorkspaceLoaderFromAppRestriction(LauncherDatabase.getAppWidgetHost(), new LayoutParserCallback<Favorites>() {
                                @Override
                                public long insertAndCheck(Favorites values) {
                                    favoritesDao.insertFavorites(values);
                                    return 1;
                                }
                            });
                        }
                        return new ArrayList<>();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void loadFavorites(Observer<List<Favorites>> observer) {
        Observable.create(new ObservableOnSubscribe<List<Favorites>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Favorites>> e) throws Exception {
                e.onNext(favoritesDao.loadFavorites());
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<Favorites>, List<Favorites>>() {
                    @Override
                    public List<Favorites> apply(List<Favorites> favorites) throws Exception {
                        return favorites;
                    }
                })
                .subscribe(observer);
    }
}
