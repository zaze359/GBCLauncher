package com.zaze.launcher.data.source;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.zaze.launcher.data.entity.Favorites;
import com.zaze.utils.ZJsonUtil;
import com.zaze.utils.log.ZLog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-01-31 - 09:21
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FavoritesRepositoryTest {
    private FavoritesRepository favoritesRepository;
    private static final String TAG = "FavoritesRepositoryTest ";

    @Before
    public void setUp() throws Exception {
        ZLog.i(TAG, "setUp");
        favoritesRepository = FavoritesRepository.getInstance(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void tearDown() throws Exception {
        ZLog.i(TAG, "tearDown");
    }

//    @Test
//    public void saveFavorites() throws Exception {
//    }

    @Test
    public void loadDefaultFavoritesIfNecessary() throws Exception {
        favoritesRepository.loadDefaultFavoritesIfNecessary()
                .subscribe(new Consumer<ArrayList<Long>>() {
                    @Override
                    public void accept(ArrayList<Long> longs) throws Exception {
                        ZLog.i(TAG, "loadDefaultFavoritesIfNecessary : %s页", longs.size());
                    }
                });

    }

    @Test
    public void loadFavorites() throws Exception {
        favoritesRepository.loadFavorites().subscribe(new Consumer<List<Favorites>>() {
            @Override
            public void accept(List<Favorites> list) throws Exception {
                ZLog.i(TAG, "loadFavorites : ", ZJsonUtil.objToJson(list));
            }
        });
    }

}