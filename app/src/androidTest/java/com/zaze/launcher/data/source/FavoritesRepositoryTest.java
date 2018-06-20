package com.zaze.launcher.data.source;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.zaze.utils.JsonUtil;
import com.zaze.utils.log.ZLog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
//    public void insertOrReplaceFavorites() throws Exception {
//    }

    @Test
    public void loadFavorites() throws Exception {
        ZLog.i(TAG, "loadFavorites : ", JsonUtil.objToJson(favoritesRepository.loadFavorites()));
    }

}