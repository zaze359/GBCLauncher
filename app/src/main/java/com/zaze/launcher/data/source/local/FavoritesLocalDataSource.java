package com.zaze.launcher.data.source.local;

import android.appwidget.AppWidgetHost;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.zaze.launcher.data.LauncherDatabase;
import com.zaze.launcher.data.dao.FavoritesDao;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.iface.FavoritesDataSource;
import com.zaze.launcher.util.LogTag;
import com.zaze.launcher.util.parser.AutoInstallsLayout;
import com.zaze.launcher.util.parser.LayoutParserCallback;
import com.zaze.utils.ZJsonUtil;
import com.zaze.utils.log.ZLog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

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
    public void saveFavorites(Favorites favorites) {
        ZLog.d(LogTag.TAG_DEBUG, "saveFavorites : " + ZJsonUtil.objToJson(favorites));
        favoritesDao.insertFavorites(favorites);
    }

    @Override
    public Observable<ArrayList<Long>> loadDefaultFavoritesIfNecessary() {
        final LayoutParserCallback callback = new LayoutParserCallback<Favorites>() {
            @Override
            public long insertAndCheck(Favorites values) {
                saveFavorites(values);
                return 1;
            }
        };
        return Observable.create(new ObservableOnSubscribe<AppWidgetHost>() {
            @Override
            public void subscribe(ObservableEmitter<AppWidgetHost> e) throws Exception {
                if (LauncherDatabase.isEmptyDatabaseCreate()) {
                    e.onNext(LauncherDatabase.getAppWidgetHost());
                }
                e.onComplete();
            }
        })
                .map(new Function<AppWidgetHost, Pair<AutoInstallsLayout, AppWidgetHost>>() {
                    @Override
                    public Pair<AutoInstallsLayout, AppWidgetHost> apply(AppWidgetHost appWidgetHost) throws Exception {
                        // From the app restrictions
                        return new Pair<>(AutoInstallsLayout.createWorkspaceLoaderFromAppRestriction(appWidgetHost, callback), appWidgetHost);
                    }
                })
                .map(new Function<Pair<AutoInstallsLayout, AppWidgetHost>, Pair<AutoInstallsLayout, AppWidgetHost>>() {
                    @Override
                    public Pair<AutoInstallsLayout, AppWidgetHost> apply(Pair<AutoInstallsLayout, AppWidgetHost> pair) throws Exception {
                        if (pair.first == null) {
                            // From a package provided by play store
                            return new Pair<>(AutoInstallsLayout.get(pair.second, callback), pair.second);
                        }
                        return pair;
                    }
                })
//                .map(new Function<Pair<AutoInstallsLayout, AppWidgetHost>, Pair<AutoInstallsLayout, AppWidgetHost>>() {
//                    @Override
//                    public Pair<AutoInstallsLayout, AppWidgetHost> apply(Pair<AutoInstallsLayout, AppWidgetHost> pair) throws Exception {
//                        if (pair.first == null) {
//                            // From a package provided by play store
//                            Context context = BaseApplication.getInstance();
//                            return new Pair<>(AutoInstallsLayout.newInstance(context, context.getPackageName(), pair.second, callback), pair.second);
//                        }
//                        return pair;
//                    }
//                })
                .map(new Function<Pair<AutoInstallsLayout, AppWidgetHost>, ArrayList<Long>>() {
                    @Override
                    public ArrayList<Long> apply(Pair<AutoInstallsLayout, AppWidgetHost> pair) throws Exception {
                        ArrayList<Long> screenIds = new ArrayList<>();
                        if (pair.first == null) {
                            // From a partner configuration APK, already in the system image
                        } else {
                            pair.first.loadLayout(screenIds);
                        }
                        LauncherDatabase.clearFlagEmptyDbCreated();
                        return screenIds;
                    }
                });
    }

    @Override
    public Observable<List<Favorites>> loadFavorites() {
        return Observable.create(new ObservableOnSubscribe<List<Favorites>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Favorites>> e) throws Exception {
                e.onNext(favoritesDao.loadFavorites());
                e.onComplete();
            }
        });
    }
}
