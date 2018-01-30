package com.zaze.launcher;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.source.FavoritesRepository;
import com.zaze.launcher.util.LauncherSharePref;
import com.zaze.launcher.util.LogTag;
import com.zaze.launcher.view.HotSeat;
import com.zaze.launcher.view.drag.DragController;
import com.zaze.utils.log.ZLog;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2017-12-17 - 23:30
 */
public class LauncherViewModel extends BaseObservable implements LauncherCallbacks {

    private static final String FIRST_RUN_ACTIVITY_DISPLAYED = "launcher.first_run_activity_displayed";
    /**
     * To avoid leaks, this must be an Application Context.
     */
    private Context mContext;
    private LauncherCallbacks mLauncherCallbacks;
    private FrameLayout.LayoutParams hotSeatLp;
    private DragController mDragController;
    private FavoritesRepository favoritesRepository;

    private final List<Disposable> disposableList = new ArrayList<>();


    public void setLauncherCallbacks(LauncherCallbacks mLauncherCallbacks) {
        this.mLauncherCallbacks = mLauncherCallbacks;
    }

    public LauncherViewModel(Context context) {
        this.mContext = context.getApplicationContext();
    }

    // --------------------------------------------------

    /**
     * 初始化一些数据
     */
    public void init() {
        mDragController = new DragController(mContext);
        favoritesRepository = FavoritesRepository.getInstance(mContext);
    }

    /**
     * 标记第一次运行界面的状态为已显示
     */
    public void markFirstRunActivityShown() {
        LauncherSharePref.apply(FIRST_RUN_ACTIVITY_DISPLAYED, true);
    }

    public boolean shouldRunFirstRunActivity() {
        return !ActivityManager.isRunningInTestHarness() &&
                !LauncherSharePref.get(FIRST_RUN_ACTIVITY_DISPLAYED, false);
    }

    /**
     * 主要是显示一些帮助信息
     */
    public void showFirstRunClings() {
    }

    public boolean onHotSeatLongClick(View hotSeat) {
        if (hotSeat instanceof HotSeat) {
            if (onLongCLickEnable()) {
                final boolean isAllAppsButton = true;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private boolean onLongCLickEnable() {
        return true;
    }

    public void loadWorkSpace() {
        favoritesRepository.loadDefaultFavoritesIfNecessary(new Observer<List<Favorites>>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableList.add(d);
            }

            @Override
            public void onNext(List<Favorites> list) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                favoritesRepository.loadFavorites(new Observer<List<Favorites>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        ZLog.i(LogTag.TAG_DEBUG, "onSubscribe");
                        disposableList.add(d);
                    }

                    @Override
                    public void onNext(List<Favorites> favorites) {
                        ZLog.i(LogTag.TAG_DEBUG, "onNext : " + favorites);
                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLog.i(LogTag.TAG_DEBUG, "onError");

                    }

                    @Override
                    public void onComplete() {
                        ZLog.i(LogTag.TAG_DEBUG, "onComplete");
                    }
                });
            }
        });
    }

    // --------------------------------------------------
    // --------------------------------------------------

    @Override
    public void preOnCreate() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "preOnCreate");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.preOnCreate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onCreate");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onCreate(savedInstanceState);
        }
    }

    @Override
    public void preOnResume() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "preOnResume");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.preOnResume();
        }
    }

    @Override
    public void onResume() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onResume");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onResume();
        }
    }

    @Override
    public void onStart() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onStart");
//        FirstFrameAnimatorHelper.setIsVisible(true);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onStart();
        }
    }

    @Override
    public void onStop() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onStop");
//        FirstFrameAnimatorHelper.setIsVisible(false);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onStop();
        }
    }

    @Override
    public void onPause() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onPause");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPause();
        }
    }

    @Override
    public void onDestroy() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onDestroy");
        synchronized (disposableList) {
            for (Disposable disposable : disposableList) {
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
            }
            disposableList.clear();
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onSaveInstanceState");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onPostCreate");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPostCreate(savedInstanceState);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onNewIntent");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onActivityResult");
//        handleActivityResult(requestCode, resultCode, data);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onRequestPermissionsResult");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onWindowFocusChanged");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onPrepareOptionsMenu");
        return false;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "dump");
    }

    @Override
    public void onHomeIntent() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onHomeIntent");
    }

    @Override
    public boolean handleBackPressed() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "handleBackPressed");
        return mLauncherCallbacks != null && mLauncherCallbacks.handleBackPressed();
    }

    @Override
    public void onTrimMemory(int level) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onTrimMemory");
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // 当所有的UI都被隐藏时
        }
    }

    @Override
    public void onLauncherProviderChange() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onLauncherProviderChange");
    }

    @Override
    public void finishBindingItems(boolean upgradePath) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "finishBindingItems");
    }

    @Override
    public void onClickAllAppsButton(View v) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onClickAllAppsButton");
    }

    @Override
    public void onClickFolderIcon(View v) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onClickFolderIcon");
    }

    @Override
    public void onClickAppShortcut(View v) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onClickAppShortcut");
    }

    @Override
    public void onClickPagedViewIcon(View v) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onClickPagedViewIcon");
    }

    @Override
    public void onClickWallpaperPicker(View v) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onClickWallpaperPicker");
    }

    @Override
    public void onClickSettingsButton(View v) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onClickSettingsButton");
    }

    @Override
    public void onClickAddWidgetButton(View v) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onClickAddWidgetButton");
    }

    @Override
    public void onPageSwitch(View newPage, int newPageIndex) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onPageSwitch");
    }

    @Override
    public void onWorkspaceLockedChanged() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onWorkspaceLockedChanged");
    }

    @Override
    public void onDragStarted(View view) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onDragStarted");
    }

    @Override
    public void onInteractionBegin() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onInteractionBegin");
    }

    @Override
    public void onInteractionEnd() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onInteractionEnd");
    }

    @Override
    public boolean forceDisableVoiceButtonProxy() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "forceDisableVoiceButtonProxy");
        return false;
    }

    @Override
    public boolean providesSearch() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "providesSearch");
        return false;
    }

    @Override
    public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "startSearch");
        return false;
    }

    @Override
    public boolean startSearchFromAllApps(String query) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "startSearchFromAllApps");
        return false;
    }

    @Override
    public void startVoice() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "startVoice");
    }

    @Override
    public boolean hasCustomContentToLeft() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "hasCustomContentToLeft");
        return false;
    }

    @Override
    public void populateCustomContentContainer() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "populateCustomContentContainer");
    }

    @Override
    public View getQsbBar() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "getQsbBar");
        return null;
    }

    @Override
    public Intent getFirstRunActivity() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "getFirstRunActivity");
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.getFirstRunActivity();
        }
        return null;
    }

    @Override
    public boolean hasFirstRunActivity() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "hasFirstRunActivity");
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasFirstRunActivity();
        }
        return false;
    }

    @Override
    public boolean hasDismissableIntroScreen() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "hasDismissableIntroScreen");
        return false;
    }

    @Override
    public View getIntroScreen() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "getIntroScreen");
        return null;
    }

    @Override
    public boolean shouldMoveToDefaultScreenOnHomeIntent() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "shouldMoveToDefaultScreenOnHomeIntent");
        return false;
    }

    @Override
    public boolean hasSettings() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "hasSettings");
        return false;
    }

    @Override
    public ComponentName getWallpaperPickerComponent() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "getWallpaperPickerComponent");
        return null;
    }

    @Override
    public boolean overrideWallpaperDimensions() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "overrideWallpaperDimensions");
        return false;
    }

    @Override
    public boolean isLauncherPreinstalled() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "isLauncherPreinstalled");
        return false;
    }

    @Override
    public boolean hasLauncherOverlay() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "hasLauncherOverlay");
        return false;
    }

    @Override
    public void setLauncherSearchCallback(Object callbacks) {
        ZLog.i(LogTag.TAG_LIFECYCLE, "setLauncherSearchCallback");
    }

}
