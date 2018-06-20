package com.zaze.launcher;

import android.app.ActivityManager;
import android.appwidget.AppWidgetHost;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.BaseObservable;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import com.zaze.launcher.compat.LauncherActivityInfoCompat;
import com.zaze.launcher.compat.LauncherAppsCompat;
import com.zaze.launcher.compat.PackageInstallerCompat;
import com.zaze.launcher.compat.StartupReceiver;
import com.zaze.launcher.compat.UserHandleCompat;
import com.zaze.launcher.compat.UserManagerCompat;
import com.zaze.launcher.data.LauncherDatabase;
import com.zaze.launcher.data.entity.AppInfo;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.entity.FolderInfo;
import com.zaze.launcher.data.entity.ItemInfo;
import com.zaze.launcher.data.entity.LauncherAppWidgetInfo;
import com.zaze.launcher.data.entity.ShortcutInfo;
import com.zaze.launcher.data.entity.WorkspaceScreen;
import com.zaze.launcher.data.source.FavoritesRepository;
import com.zaze.launcher.data.source.WorkspaceScreenRepository;
import com.zaze.launcher.util.DeferredHandler;
import com.zaze.launcher.util.LauncherSharePref;
import com.zaze.launcher.util.LogTag;
import com.zaze.launcher.util.LongArrayMap;
import com.zaze.launcher.util.Utilities;
import com.zaze.launcher.util.parser.AutoInstallsLayout;
import com.zaze.launcher.util.parser.CommonAppTypeParser;
import com.zaze.launcher.util.parser.DefaultLayoutParser;
import com.zaze.launcher.util.parser.LayoutParserCallback;
import com.zaze.launcher.view.Folder;
import com.zaze.launcher.view.FolderIcon;
import com.zaze.launcher.view.HotSeat;
import com.zaze.launcher.view.IconCache;
import com.zaze.launcher.view.PagedView;
import com.zaze.launcher.view.Workspace;
import com.zaze.launcher.view.drag.DragController;
import com.zaze.utils.log.ZLog;
import com.zaze.utils.log.ZTag;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Description :
 * 懒, 不抽取接口了, 直接写实现类
 *
 * @author : ZAZE
 * @version : 2017-12-17 - 23:30
 */
public class LauncherPresenter extends BaseObservable
        implements LauncherContract.Presenter, LauncherCallbacks {

    private static final String FIRST_RUN_ACTIVITY_DISPLAYED = "launcher.first_run_activity_displayed";
    private static final long INVALID_SCREEN_ID = -1L;

    private static final boolean REMOVE_UNRESTORED_ICONS = true;

    /**
     * batch size for the workspace icons
     * workspace icons 批处理数量
     */
    private static final int ITEMS_CHUNK = 6;

    /**
     *
     */
    public static final int LOADER_FLAG_NONE = 0;
    public static final int LOADER_FLAG_CLEAR_WORKSPACE = 1;
    public static final int LOADER_FLAG_MIGRATE_SHORTCUTS = 1 << 1;

    /**
     *
     */
    private LauncherContract.View mView;
    private FavoritesRepository favoritesRepository;
    private WorkspaceScreenRepository workspaceScreenRepository;

    private LauncherCallbacks mLauncherCallbacks;
    private FrameLayout.LayoutParams hotSeatLp;
    private DragController mDragController;
    private IconCache mIconCache;

    final LauncherAppsCompat mLauncherApps;
    final UserManagerCompat mUserManager;

    private boolean mPaused = true;

    private LoaderTask mLoaderTask;
    boolean mIsLoaderTaskRunning;
    boolean mHasLoaderCompletedOnce;

    /**
     * We start off with everything not loaded.
     * After that, we assume that our monitoring of the package manager
     * provides all updates and we never need to do a requery.
     * These are only ever touched from the loader thread.
     * <p>
     * 我们从任何东西没有被加载开始.
     * 在此之后，我们假设我们对包管理器的监控提供了所有的更新, 那么我们不需要再做一次请求,
     * 请求这些曾经在加载线程中获取过的
     */
    boolean mWorkspaceLoaded;
    boolean mAllAppsLoaded;
    // --------------------------------------------------

    final Object mLock = new Object();
    /**
     * The lock that must be acquired before referencing any static bg data structures.
     * Unlike other locks, this one can generally be held long-term because we never expect any of these
     * static data structures to be referenced outside of the worker thread
     * except on the first load after configuration change.
     * 在引用任何静态bg数据之前必须获得这个锁。
     * 不同于其他的锁, 它通常可以被长期持有, 因为我们从来不期望这些静态数据在worker thread 之外被引用
     * 除了配置发生变化后的第一次加载
     **/
    static final Object sBgLock = new Object();

    /**
     * To avoid leaks, this must be an Application Context.
     */
    private Context mContext;
    DeferredHandler mHandler = new DeferredHandler();
    static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");

    static {
        sWorkerThread.start();
    }

    static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    /**
     * sBgItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
     * LauncherModel to their ids
     */
    static final LongArrayMap<ItemInfo> sBgItemsIdMap = new LongArrayMap<>();

    /**
     * sBgWorkspaceItems is passed to bindItems, which expects a list of all folders and shortcuts
     * created by LauncherModel that are directly on the home screen (however, no widgets or
     * shortcuts within folders).
     */
    static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<>();

    /**
     * sBgAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
     */
//    static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets = new ArrayList<LauncherAppWidgetInfo>();

    /**
     * sBgFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
     */
    private static final LongArrayMap<FolderInfo> sBgFolders = new LongArrayMap<>();

    /**
     * sBgWorkspaceScreens is the ordered set of workspace screens.
     */
    static final ArrayList<Long> sBgWorkspaceScreens = new ArrayList<>();

    /**
     * sPendingPackages is a set of packages which could be on sdcard and are not available yet
     * 这是一个 可能在sdcard中但是还不可用的应用的 package 集合
     */
    static final HashMap<UserHandleCompat, HashSet<String>> sPendingPackages = new HashMap<>();


    /**
     * When we are loading pages synchronously, we can't just post the binding of items on the side pages as this delays the rotation process.
     * Instead, we wait for a callback from the first draw (in Workspace) to initiate the binding of the remaining side pages.
     * Any time we start a normal load, we also clear this set of Runnables.
     * <p>
     * 当我们同步加载页面时, 我们不能只在页面上绑定items，这将会延迟转换的过程
     * 相反, 我们等待工作区第一次绘制完成后的回调, 去开始绑定剩下的页面。
     * 任何时候我们开始一个正常的加载, 我们也会清空这组runnable队列
     */
    static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<>();
    /**
     * Set of runnables to be called on the background thread after the workspace binding
     * is complete.
     * 在workspace binding 完成后被调用
     */
    static final ArrayList<Runnable> mBindCompleteRunnables = new ArrayList<>();

    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();
    private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<Runnable>();


    public void setLauncherCallbacks(LauncherCallbacks mLauncherCallbacks) {
        this.mLauncherCallbacks = mLauncherCallbacks;
    }

    public LauncherPresenter(Context context) {
        mContext = context.getApplicationContext();
        mLauncherApps = LauncherAppsCompat.getInstance(mContext);
        mUserManager = UserManagerCompat.getInstance(mContext);
        mIconCache = LauncherAppState.getInstance().getIconCache();
    }

    // --------------------------------------------------

    /**
     * 初始化一些数据
     */
    public void initialize(LauncherContract.View view) {
        this.mView = view;
        mDragController = new DragController(mContext);
        favoritesRepository = FavoritesRepository.getInstance(mContext);
        workspaceScreenRepository = WorkspaceScreenRepository.getInstance(mContext);
        mPaused = false;
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

    public void startLoader(int synchronousBindPage) {
        startLoader(synchronousBindPage, LOADER_FLAG_NONE);
    }

    private void startLoader(int synchronousBindPage, int loadFlags) {
        // Clear any deferred bind-runnables from the synchronized load process
        // We must do this before any loading/binding is scheduled below.
        synchronized (mLock) {
            synchronized (mDeferredBindRunnables) {
                mDeferredBindRunnables.clear();
            }
            // If there is already one running, tell it to stop.
            stopLoaderLocked();
            mLoaderTask = new LoaderTask(loadFlags);
            if (synchronousBindPage != PagedView.INVALID_RESTORE_PAGE
                    && mAllAppsLoaded && mWorkspaceLoaded && !mIsLoaderTaskRunning) {
                mLoaderTask.runBindSynchronousPage(synchronousBindPage);
            } else {
                sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                sWorker.post(mLoaderTask);
            }
        }
    }

    /**
     * If there is already a loader task running, tell it to stop.
     */
    private void stopLoaderLocked() {
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            oldTask.stopLocked();
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }


    public void setWorkspaceLoading(boolean isLoading) {
        // TODO setWorkspaceLoading
        ZLog.e(ZTag.TAG_DEBUG, "TODO : setWorkspaceLoading : " + isLoading);

    }

    // --------------------------------------------------

    ShortcutInfo getShortcutInfo(Context context, @NonNull Favorites favorites) {
        final ShortcutInfo info = new ShortcutInfo();
        // TODO: If there's an explicit component and we can't install that, delete it.
        info.title = Utilities.trim(favorites.getTitle());
        Bitmap icon = Utilities.loadIcon(context, favorites, info);
        // the fallback icon
        if (icon == null) {
            icon = mIconCache.getDefaultIcon(info.user);
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);
        return info;
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     * 如果favorites 不为空,将会被用来填充缺失的数据, 比如title和icon
     */
    public ShortcutInfo getAppShortcutInfo(Context context, @NonNull Intent intent,
                                           UserHandleCompat user, @Nullable Favorites favorites,
                                           boolean allowMissingTarget, boolean useLowResIcon) {
        if (user == null) {
            ZLog.d(LogTag.TAG_LOADER, "Null user found in getAppShortcutInfo");
            return null;
        }
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            ZLog.d(LogTag.TAG_LOADER, "Missing component found in getAppShortcutInfo : %s", componentName);
            return null;
        }
        //
        Intent newIntent = new Intent(intent.getAction(), null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setComponent(componentName);
        // 解析Intent
        LauncherActivityInfoCompat lai = mLauncherApps.resolveActivity(newIntent, user);
        if (lai == null && !allowMissingTarget) {
            ZLog.e(LogTag.TAG_LOADER, "Missing activity found in getShortcutInfo : %s", componentName);
            return null;
        }
        ShortcutInfo info = new ShortcutInfo();
        mIconCache.getTitleAndIcon(info, componentName, lai, user, false, useLowResIcon);
        if (mIconCache.isDefaultIcon(info.getIcon(mIconCache), user) && favorites != null) {
            Bitmap icon = Utilities.createIconBitmap(context, favorites);
            info.setIcon(icon == null ? mIconCache.getDefaultIcon(user) : icon);
        }

        // from the db
        if (TextUtils.isEmpty(info.title) && favorites != null) {
            info.title = Utilities.trim(favorites.getTitle());
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION;
        info.user = user;
        info.contentDescription = mUserManager.getBadgedLabelForUser(info.title, info.user);
        if (lai != null) {
            info.flags = AppInfo.initFlags(lai);
        }
        return info;
    }

    public ShortcutInfo getRestoredItemInfo() {
        // TODO: 2018/3/1
        return null;
    }

    Intent getRestoredItemIntent() {
        // TODO: 2018/3/1
        return null;
    }

    // check & update map of what's occupied; used to discard overlapping/invalid items
    // 检查并更新map哪些被占用，用于去除重复项或者无效项
    private boolean checkItemPlacement(LongArrayMap<ItemInfo[][]> occupied, ItemInfo item,
                                       ArrayList<Long> workspaceScreens) {
        // TODO: 2018/3/1
        return true;
    }

    static FolderInfo findOrMakeFolder(LongArrayMap<FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }

    /**
     * Clears all the sBg data structures
     */
    private void clearSBgDataStructures() {
        synchronized (sBgLock) {
            sBgWorkspaceItems.clear();
//            sBgAppWidgets.clear();
            sBgFolders.clear();
            sBgItemsIdMap.clear();
            sBgWorkspaceScreens.clear();
        }
    }

    /**
     * Update the order of the workspace screens in the database. The array list contains
     * a list of screen ids in the order that they should appear.
     */
    public void updateWorkspaceScreenOrder(Context context, ArrayList<Long> sBgWorkspaceScreens) {

    }


    /**
     * Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler.
     * 主线程中执行
     */
    void runOnMainThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            // 如果当前在工作线程, 则传递到主线程中执行
            mHandler.post(r);
        } else {
            r.run();
        }
    }

    /**
     * Runs the specified runnable immediately if called from the worker thread, otherwise it is
     * posted on the worker thread handler.
     */
    static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            // 如果当前不在worker线程，则传递到worker线程中执行
            sWorker.post(r);
        }
    }

    public boolean waitUntilResume(Runnable run) {
        return waitUntilResume(run, false);
    }

    boolean waitUntilResume(Runnable run, boolean deletePreviousRunnables) {
        if (mPaused) {
            ZLog.d(LogTag.TAG_LOADER, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mBindOnResumeCallbacks.remove(run)) {
                }
            }
            mBindOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
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
        mPaused = false;
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
        mPaused = true;
        ZLog.i(LogTag.TAG_LIFECYCLE, "onPause");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPause();
        }
    }

    @Override
    public void onDestroy() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "onDestroy");
        stopLoader();
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
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasCustomContentToLeft();
        }
        return false;
    }

    @Override
    public void populateCustomContentContainer() {
        ZLog.i(LogTag.TAG_LIFECYCLE, "populateCustomContentContainer");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.populateCustomContentContainer();
        }
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
    // --------------------------------------------------
    // --------------------------------------------------


    /**
     * Loads the workspace screen ids in an ordered list.
     */
    public static ArrayList<Long> loadWorkspaceScreensDb(Context context) {
        ArrayList<Long> screenIds = new ArrayList<>();
        List<WorkspaceScreen> list = WorkspaceScreenRepository.getInstance(context).loadWorkspaceScreens();
        if (!list.isEmpty()) {
            for (WorkspaceScreen workspaceScreen : list) {
                screenIds.add(workspaceScreen.getId());
            }
        }
        return screenIds;
    }


    // --------------------------------------------------
    // --------------------------------------------------

    /**
     * 加载任务
     */
    private class LoaderTask implements Runnable {
        private int loadFlags;
        private boolean mStopped;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mLoadAndBindStepFinished;

        public LoaderTask(int loadFlags) {
            this.loadFlags = loadFlags;
        }

        @Override
        public void run() {
            synchronized (mLock) {
                if (mStopped) {
                    return;
                }
                mIsLoaderTaskRunning = true;
            }
            // Optimize for end-user experience: if the Launcher is up and // running with the
            // All Apps interface in the foreground, load All Apps first. Otherwise, load the
            // workspace first (default).
            keep_running:
            {
                ZLog.d(ZTag.TAG_DEBUG, "step 1: loading workspace");
                loadAndBindWorkspace();
                if (mStopped) {
                    break keep_running;
                }
                waitForIdle();
                // second step
                ZLog.d(ZTag.TAG_DEBUG, "step 2: loading all apps");
                loadAndBindAllApps();
            }
            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
                mHasLoaderCompletedOnce = true;
            }
        }

        /**
         * 加载绑定工作区
         */
        private void loadAndBindWorkspace() {
            mIsLoadingAndBindingWorkspace = true;
            ZLog.i(LogTag.TAG_LOADER, " loadAndBindWorkspace mWorkspaceLoaded = " + mWorkspaceLoaded);
            if (!mWorkspaceLoaded) {
                // Load the workspace
                loadWorkspace();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        ZLog.d(LogTag.TAG_LOADER, "loadAndBindWorkspace stopped");
                        return;
                    }
                    mWorkspaceLoaded = true;
                }
            }
            // Bind the workspace
            bindWorkspace(-1);
        }

        /**
         * Wait until the either we're stopped or the other threads are done.
         * This way we don't start loading all apps until the workspace has settled down.
         * 等待直到空闲我们停止了或者其他线程结束了
         * 这样我们就不会开始加载所有的应用程序，直到工作空间稳定下来
         */
        private void waitForIdle() {
            synchronized (LoaderTask.this) {
                ZLog.d(LogTag.TAG_LOADER, "waitForIdle");
                final long workspaceWaitTime = SystemClock.uptimeMillis();
                mHandler.postIdle(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (LoaderTask.this) {
                            mLoadAndBindStepFinished = true;
                            ZLog.d(ZTag.TAG_DEBUG, "done with previous binding step");
                            LoaderTask.this.notify();
                        }
                    }
                });
                while (!mStopped && !mLoadAndBindStepFinished) {
                    try {
                        // Just in case mFlushingWorkerThread changes but we aren't woken up,
                        // wait no longer than 1sec at a time
                        // ???万一mFlushingWorkerThread发生了变化, 但是我们没有被唤醒
                        // 等待时间不超过一秒
                        this.wait(1000);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                ZLog.d(ZTag.TAG_DEBUG, "waited "
                        + (SystemClock.uptimeMillis() - workspaceWaitTime)
                        + "ms for previous step to finish binding");
            }
        }

        /**
         * 加载工作区
         */
        public void loadWorkspace() {
            ZLog.i(LogTag.TAG_LOADER, "-------- loadWorkspace : 加载工作区");
            Context context = MyApplication.getInstance().getApplicationContext();
            final PackageManager packageManager = mContext.getPackageManager();
            final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
            final LongSparseArray<UserHandleCompat> allUsers = new LongSparseArray<>();
            final boolean isSdCardReady = context.registerReceiver(null,
                    new IntentFilter(StartupReceiver.SYSTEM_READY)) != null;

            for (UserHandleCompat user : mUserManager.getUserProfiles()) {
                allUsers.put(mUserManager.getSerialNumberForUser(user), user);
            }
            // TODO do something
            //
            loadDefaultFavoritesIfNecessary();
            //
            synchronized (sBgLock) {
                clearSBgDataStructures();
                final HashMap<String, Integer> installingPkgs = PackageInstallerCompat
                        .getInstance(mContext).updateAndGetActiveSessionCache();
                sBgWorkspaceScreens.addAll(loadWorkspaceScreensDb(mContext));
                final ArrayList<Long> itemsToRemove = new ArrayList<>();
                final ArrayList<Long> restoredRows = new ArrayList<>();
                final LongArrayMap<ItemInfo[][]> occupied = new LongArrayMap<>();

                List<Favorites> favoritesList = favoritesRepository.loadFavorites();
                ShortcutInfo shortcutInfo;
                int container;
                long id;
                long serialNumber;
                Intent intent;
                UserHandleCompat user;
//
//                boolean itemReplaced = false;
//                int disabledState = ShortcutInfo.DEFAULT;
//                container = (int) favorites.getContainer();
                ZLog.i(LogTag.TAG_LOADER, "总共有%s条收藏", favoritesList.size());

                for (Favorites favorites : favoritesList) {
                    if (mStopped) {
                        ZLog.w(LogTag.TAG_LOADER, "当前Task线程已停止, 终止处理 : " + favorites);
                        break;
                    }
                    ZLog.d(LogTag.TAG_LOADER, "处理Favorites : %s", favorites);
                    id = favorites.getId();
                    int itemType = favorites.getItemType();
                    int promiseType = favorites.getRestored();
                    boolean restored = 0 != promiseType;
                    boolean allowMissingTarget = false;
                    container = (int) favorites.getContainer();
                    switch (itemType) {
                        case LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.ItemColumns.ITEM_TYPE_SHORTCUT:
                            serialNumber = favorites.getProfileId();
                            user = allUsers.get(serialNumber);
                            boolean itemReplaced = false;
                            int disabledState = 0;
                            if (user == null) {
                                // 对应用户已经被删除, 移除
                                itemsToRemove.add(id);
                                break;
                            }
                            try {
                                intent = Intent.parseUri(favorites.getIntent(), 0);
                                ComponentName cn = intent.getComponent();
                                if (cn != null && !TextUtils.isEmpty(cn.getPackageName())) {
                                    boolean validPkg = mLauncherApps.isPackageEnabledForProfile(cn.getPackageName(), user);
                                    boolean validComponent = validPkg && mLauncherApps.isActivityEnabledForProfile(cn, user);
                                    if (validComponent) {
                                        // TODO ???????
                                        if (restored) {
                                            // no special handling necessary for this item
//                                                restoredRows.add(id);
                                            restored = false;
                                        }
                                    } else if (validPkg) {
                                        intent = null;
                                        if ((promiseType & ShortcutInfo.FLAG_AUTOINTALL_ICON) != 0) {
                                            // We allow auto install apps to have their intent
                                            // updated after an install.
                                            intent = packageManager.getLaunchIntentForPackage(cn.getPackageName());
                                            if (intent != null) {
                                                favorites.setIntent(intent.toUri(0));
                                                favoritesRepository.insertOrReplaceFavorites(favorites);
                                            }
                                        }
                                        if (intent == null) {
                                            // 应用已安装，但是不可用
                                            ZLog.w(LogTag.TAG_ERROR, "Invalid component removed: " + favorites.getIntent());
                                            itemsToRemove.add(id);
                                            break;
                                        } else {
                                            restoredRows.add(id);
                                            restored = false;
                                        }
                                    } else if (restored) {
                                        // 当前包还不可用，但是也许过会将会被安装
                                        ZLog.w(ZTag.TAG_DEBUG, "package not yet restored: " + cn);
                                        if ((promiseType & ShortcutInfo.FLAG_RESTORE_STARTED) != 0) {
                                            // 已经开始恢复一次
                                        } else if (installingPkgs.containsKey(cn.getPackageName())) {
                                            // App已经开始恢复, 更新flag
                                            promiseType |= ShortcutInfo.FLAG_RESTORE_STARTED;
                                            favorites.setRestored(promiseType);
                                            favoritesRepository.insertOrReplaceFavorites(favorites);
                                        } else if ((promiseType & ShortcutInfo.FLAG_RESTORED_APP_TYPE) != 0) {
                                            // 这是一个常用的应用, 尝试替换
                                            int appType = CommonAppTypeParser.decodeItemTypeFromFlag(promiseType);
                                            CommonAppTypeParser parser = new CommonAppTypeParser(id, appType, context);
                                            if (parser.findDefaultApp()) {
                                                // Default app found. Replace it.
                                                intent = parser.parsedIntent;
                                                cn = intent.getComponent();
                                                Favorites saved = parser.parsedValues;
                                                saved.setId(id);
                                                favoritesRepository.insertOrReplaceFavorites(saved);
                                                restored = false;
                                                itemReplaced = true;
                                            } else if (REMOVE_UNRESTORED_ICONS) {
                                                ZLog.d(LogTag.TAG_DEBUG, "Unrestored package removed: " + cn + " -->>" + true);
                                                itemsToRemove.add(id);
                                                continue;
                                            }
                                        } else if (REMOVE_UNRESTORED_ICONS) {
                                            ZLog.d(LogTag.TAG_DEBUG, "Unrestored package removed: " + cn + " -->>" + true);
                                            itemsToRemove.add(id);
                                            continue;
                                        }
                                    } else if (launcherApps.isAppEnabled(packageManager,
                                            cn.getPackageName(),
                                            PackageManager.GET_UNINSTALLED_PACKAGES)) {
                                        // Package is present but not available.
                                        allowMissingTarget = true;
                                        disabledState = ShortcutInfo.FLAG_DISABLED_NOT_AVAILABLE;
                                    } else if (!isSdCardReady) {
                                        // sdcard 还没有准备好, 一旦它准备好，包可能就可用了
                                        ZLog.w(ZTag.TAG_DEBUG, "Invalid package: " + cn
                                                + " (check again later)");
                                        HashSet<String> pkgs = sPendingPackages.get(user);
                                        if (pkgs == null) {
                                            pkgs = new HashSet<>();
                                            sPendingPackages.put(user, pkgs);
                                        }
                                        pkgs.add(cn.getPackageName());
                                        allowMissingTarget = true;
                                        // Add the icon on the workspace anyway.
                                    } else {
                                        // 不要等待外部媒体加载。记录无效的包，并删除它
                                        ZLog.w(ZTag.TAG_DEBUG, "Invalid package removed: " + cn);
                                        itemsToRemove.add(id);
                                        continue;
                                    }
                                } else if (cn == null) {
                                    // For shortcuts with no component, keep them as they are
                                    restoredRows.add(id);
                                    restored = false;
                                }
                            } catch (URISyntaxException e) {
                                ZLog.w(LogTag.TAG_ERROR, "Invalid uri: " + favorites.getIntent());
                                itemsToRemove.add(id);
                                break;
                            }
                            boolean useLowResIcon = container >= 0 && favorites.getRank() >= FolderIcon.NUM_ITEMS_IN_PREVIEW;
                            if (itemReplaced) {
                                if (user.equals(UserHandleCompat.myUserHandle())) {
                                    shortcutInfo = getAppShortcutInfo(context, intent, user, favorites,
                                            false, useLowResIcon);
                                } else {
                                    // Don't replace items for other profiles.
                                    itemsToRemove.add(id);
                                    break;
                                }
                            } else if (restored) {
                                if (user.equals(UserHandleCompat.myUserHandle())) {
                                    ZLog.d(LogTag.TAG_LOADER, "constructing info for partially restored package");
                                    shortcutInfo = getRestoredItemInfo();
                                    intent = getRestoredItemIntent();
                                } else {
                                    // Don't restore items for other profiles.
                                    itemsToRemove.add(id);
                                    break;
                                }
                            } else if (itemType == LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION) {
                                shortcutInfo = getAppShortcutInfo(context, intent, user, favorites,
                                        allowMissingTarget, useLowResIcon);
                            } else {
                                shortcutInfo = getShortcutInfo(context, favorites);
                                // App shortcuts that used to be automatically added to Launcher
                                // didn't always have the correct intent flags set, so do that
                                // here
                                if (intent.getAction() != null &&
                                        intent.getCategories() != null &&
                                        intent.getAction().equals(Intent.ACTION_MAIN) &&
                                        intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                }
                            }
                            if (shortcutInfo != null) {
                                shortcutInfo.setValues(favorites);
                                shortcutInfo.intent = intent;
                                shortcutInfo.intent.putExtra(ItemInfo.EXTRA_PROFILE, serialNumber);
                                if (shortcutInfo.promisedIntent != null) {
                                    shortcutInfo.promisedIntent.putExtra(ItemInfo.EXTRA_PROFILE, serialNumber);
                                }
                                shortcutInfo.isDisabled = disabledState;
                                // TODO 安全模式
//                                        if (isSafeMode && !Utilities.isSystemApp(context, intent)) {
//                                            shortcutInfo.isDisabled |= ShortcutInfo.FLAG_DISABLED_SAFEMODE;
//                                        }
                                // check & update map of what's occupied
                                if (!checkItemPlacement(occupied, shortcutInfo, sBgWorkspaceScreens)) {
                                    itemsToRemove.add(id);
                                    break;
                                }
                                if (restored) {
                                    ComponentName cn = shortcutInfo.getTargetComponent();
                                    if (cn != null) {
                                        Integer progress = installingPkgs.get(cn.getPackageName());
                                        if (progress != null) {
                                            shortcutInfo.setInstallProgress(progress);
                                        } else {
                                            shortcutInfo.status &= ~ShortcutInfo.FLAG_INSTALL_SESSION_ACTIVE;
                                        }
                                    }
                                }
                                switch (container) {
                                    case LauncherSettings.ItemColumns.CONTAINER_DESKTOP:
                                    case LauncherSettings.ItemColumns.CONTAINER_HOT_SEAT:
                                        sBgWorkspaceItems.add(shortcutInfo);
                                        break;
                                    default:
                                        // Item is in a user folder
                                        FolderInfo folderInfo = findOrMakeFolder(sBgFolders, container);
                                        folderInfo.add(shortcutInfo);
                                        break;
                                }
                                sBgItemsIdMap.put(shortcutInfo.id, shortcutInfo);
                            } else {
                                throw new RuntimeException("Unexpected null ShortcutInfo");
                            }
                            break;
                        case LauncherSettings.ItemColumns.ITEM_TYPE_FOLDER:
                            FolderInfo folderInfo = findOrMakeFolder(sBgFolders, id);
                            folderInfo.setValues(favorites);
                            // check & update map of what's occupied
                            if (!checkItemPlacement(occupied, folderInfo, sBgWorkspaceScreens)) {
                                itemsToRemove.add(id);
                                break;
                            }
                            switch (container) {
                                case LauncherSettings.ItemColumns.CONTAINER_DESKTOP:
                                case LauncherSettings.ItemColumns.CONTAINER_HOT_SEAT:
                                    sBgWorkspaceItems.add(folderInfo);
                                    break;
                                default:
                                    break;
                            }

                            if (restored) {
                                // no special handling required for restored folders
                                restoredRows.add(id);
                            }
                            sBgItemsIdMap.put(folderInfo.id, folderInfo);
                            sBgFolders.put(folderInfo.id, folderInfo);
                            break;
                        case LauncherSettings.ItemColumns.ITEM_TYPE_APPWIDGET:
                        case LauncherSettings.ItemColumns.ITEM_TYPE_CUSTOM_APPWIDGET:
                            // TODO APPWIDGET
                            break;
                        default:
                            break;
                    }
                }
                ZLog.i(LogTag.TAG_LOADER, "移除无效item");
                if (!itemsToRemove.isEmpty()) {
                    favoritesRepository.deleteFavorites(itemsToRemove);
                }
                ZLog.i(LogTag.TAG_LOADER, "移除空目录");
                for (long folderId : favoritesRepository.deleteEmptyFolders()) {
                    sBgWorkspaceItems.remove(sBgFolders.get(folderId));
                    sBgFolders.remove(folderId);
                    sBgItemsIdMap.remove(folderId);
                }
                // 对所有folder的content进行排序，并且确保前3个item是高分辨率的
                // Sort all the folder items and make sure the first 3 items are high resolution.
                for (FolderInfo folder : sBgFolders) {
                    Collections.sort(folder.contents, Folder.ITEM_POS_COMPARATOR);
                    int pos = 0;
                    for (ShortcutInfo info : folder.contents) {
                        if (info.usingLowResIcon) {
                            info.updateIcon(mIconCache, false);
                        }
                        pos++;
                        if (pos >= FolderIcon.NUM_ITEMS_IN_PREVIEW) {
                            break;
                        }
                    }
                }
                if (restoredRows.size() > 0) {
                    // Update restored items that no longer require special handling
                    // 更新需要恢复的items 不再需要特殊处理
                    favoritesRepository.restoredRows(restoredRows);
                }
                // TODO
                //                        if (!isSdCardReady && !sPendingPackages.isEmpty()) {
//                            context.registerReceiver(new AppsAvailabilityCheck(),
//                                    new IntentFilter(StartupReceiver.SYSTEM_READY),
//                                    null, sWorker);
//                        }
                ZLog.i(LogTag.TAG_LOADER, "移除空的分屏");
                ArrayList<Long> unusedScreens = new ArrayList<>(sBgWorkspaceScreens);
                for (ItemInfo item : sBgItemsIdMap) {
                    long screenId = item.screenId;
                    if (item.container == LauncherSettings.ItemColumns.CONTAINER_DESKTOP &&
                            unusedScreens.contains(screenId)) {
                        unusedScreens.remove(screenId);
                    }
                }
                // If there are any empty screens remove them, and update.
                if (unusedScreens.size() != 0) {
                    sBgWorkspaceScreens.removeAll(unusedScreens);
                    updateWorkspaceScreenOrder(mContext, sBgWorkspaceScreens);
                }
            }
        }


        /**
         * 加载默认收藏配置
         * Loads the default workspace based on the following priority scheme:
         * 1) From the app restrictions
         * 2) From a package provided by play store
         * 3) From a partner configuration APK, already in the system image
         * 4) The default configuration for the particular device
         *
         * @return screenIds
         */
        synchronized public void loadDefaultFavoritesIfNecessary() {
            if (LauncherDatabase.isEmptyDatabaseCreate()) {
                LayoutParserCallback callback = new LayoutParserCallback<Favorites>() {
                    @Override
                    public long insertAndCheck(Favorites values) {
                        favoritesRepository.insertOrReplaceFavorites(values);
                        return 1;
                    }
                };
                AppWidgetHost appWidgetHost = LauncherDatabase.getAppWidgetHost();
                //  From the app restrictions
                AutoInstallsLayout autoInstallsLayout = AutoInstallsLayout.createWorkspaceLoaderFromAppRestriction(appWidgetHost, callback);
                if (autoInstallsLayout == null) {
                    // From a package provided by play store
                    autoInstallsLayout = AutoInstallsLayout.get(appWidgetHost, callback);
                }
                if (autoInstallsLayout == null) {
                    // From a partner configuration APK, already in the system image
//                    final Partner partner = Partner.get(getContext().getPackageManager());
//                    if (partner != null && partner.hasDefaultLayout()) {
//                        final Resources partnerRes = partner.getResources();
//                        int workspaceResId = partnerRes.getIdentifier(Partner.RES_DEFAULT_LAYOUT,
//                                "xml", partner.getPackageName());
//                        if (workspaceResId != 0) {
//                            loader = new DefaultLayoutParser(getContext(), mOpenHelper.mAppWidgetHost,
//                                    mOpenHelper, partnerRes, workspaceResId);
//                        }
//                    }
                }
                if (autoInstallsLayout == null) {
                    // The default configuration for the particular device
                    Context context = LauncherApplication.getInstance();
                    int defaultLayout = LauncherAppState.getInstance()
                            .getInvariantDeviceProfile().defaultLayoutId;
                    autoInstallsLayout = new DefaultLayoutParser(context, appWidgetHost,
                            callback, context.getResources(), defaultLayout);
                }
                // Populate favorites table with initial favorites
                ArrayList<Long> screenIds = new ArrayList<>();
                autoInstallsLayout.loadLayout(screenIds);
                workspaceScreenRepository.saveWorkspaceScreen(screenIds);
                LauncherDatabase.clearFlagEmptyDbCreated();
            }
        }


        /**
         * 绑定工作区数据到视图
         *
         * @param synchronizeBindPage
         */
        private void bindWorkspace(int synchronizeBindPage) {
            final long t = SystemClock.uptimeMillis();
            Runnable r;
            final LauncherContract.View oldView = mView;
            if (oldView == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                ZLog.w(LogTag.TAG_LOADER, "LoaderTask running with no launcher");
                return;
            }
            ZLog.i(LogTag.TAG_LOADER, "-------- bindWorkspace : 绑定工作区数据到视图");
            // 拷贝数据进行处理
            ArrayList<ItemInfo> workspaceItems = new ArrayList<>();
//            ArrayList<LauncherAppWidgetInfo> appWidgets = new ArrayList<LauncherAppWidgetInfo>();
            ArrayList<Long> orderedScreenIds = new ArrayList<>();
            final LongArrayMap<FolderInfo> folders;
            final LongArrayMap<ItemInfo> itemsIdMap;
            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
//                appWidgets.addAll(sBgAppWidgets);
                orderedScreenIds.addAll(sBgWorkspaceScreens);
                folders = sBgFolders.clone();
                itemsIdMap = sBgItemsIdMap.clone();
            }
            // TODO ???
            // 若synchronizeBindPage != INVALID_RESTORE_PAGE 则表示同步加载
            final boolean isLoadingSynchronously = synchronizeBindPage != PagedView.INVALID_RESTORE_PAGE;
            // 计算需要bind到的屏幕, 若是同步加载则 currScreen = synchronizeBindPage, 否则就是当前工作区的屏幕
            int currScreen = isLoadingSynchronously ? synchronizeBindPage : mView.getCurrentWorkspaceScreen();
            if (currScreen >= orderedScreenIds.size()) {
                // 这里可能不是工作区(是hotSeat 或者 一个空页), 将状态置为INVALID_RESTORE_PAGE
                currScreen = PagedView.INVALID_RESTORE_PAGE;
            }
            final int currentScreen = currScreen;
            // 从orderedScreenIds中获取screenId
            final long currentScreenId = currentScreen < 0 ? INVALID_SCREEN_ID : orderedScreenIds.get(currentScreen);
            // 首先加载当前页所有项(在此过程中, 调用startBinding()之前，我们先将所有在工作区中存在的项解绑)
            unbindWorkspaceItemsOnMainThread();
            // 先将当前屏幕的项和所有其他剩余的项分开
            ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<>();
            ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<>();
            ArrayList<LauncherAppWidgetInfo> currentAppWidgets = new ArrayList<>();
            ArrayList<LauncherAppWidgetInfo> otherAppWidgets = new ArrayList<>();
            LongArrayMap<FolderInfo> currentFolders = new LongArrayMap<>();
            LongArrayMap<FolderInfo> otherFolders = new LongArrayMap<>();

            filterCurrentWorkspaceItems(currentScreenId, workspaceItems, currentWorkspaceItems, otherWorkspaceItems);
//            filterCurrentAppWidgets(currentScreenId, appWidgets, currentAppWidgets, otherAppWidgets);
            filterCurrentFolders(currentScreenId, itemsIdMap, folders, currentFolders, otherFolders);
            sortWorkspaceItemsSpatially(currentWorkspaceItems);
            sortWorkspaceItemsSpatially(otherWorkspaceItems);
            // 告诉工作区 , 我们准备开始绑定
            startBinding(oldView);
            //
            bindWorkspaceScreens(oldView, orderedScreenIds);
            // 加载items到当前页
            bindWorkspaceItems(oldView, currentWorkspaceItems, currentAppWidgets, currentFolders, null);
            if (isLoadingSynchronously) {
                r = new Runnable() {
                    @Override
                    public void run() {
                        LauncherContract.View view = tryGetView(oldView);
                        if (view != null && currentScreen != PagedView.INVALID_RESTORE_PAGE) {
                            view.onPageBoundSynchronously(currentScreen);
                        }
                    }
                };
                runOnMainThread(r);
            }
            // 在首页加载完成后, 加载所有剩下的页
            synchronized (mDeferredBindRunnables) {
                mDeferredBindRunnables.clear();
            }
            bindWorkspaceItems(oldView, otherWorkspaceItems, otherAppWidgets, otherFolders,
                    (isLoadingSynchronously ? mDeferredBindRunnables : null));
            // 通知workspace 我们绑定完成
            r = new Runnable() {
                @Override
                public void run() {
                    LauncherContract.View view = tryGetView(oldView);
                    if (view != null) {
                        view.finishBindingItems();
                    }
                    mIsLoadingAndBindingWorkspace = false;
                    // Run all the bind complete runnables after workspace is bound.
                    // 在工作区绑定之后, 执行所有的 bind complete runnables
                    if (!mBindCompleteRunnables.isEmpty()) {
                        for (final Runnable r : mBindCompleteRunnables) {
                            runOnWorkerThread(r);
                        }
                        mBindCompleteRunnables.clear();
                    }
                    ZLog.d(LogTag.TAG_LOADER, "bound workspace in "
                            + (SystemClock.uptimeMillis() - t) + "ms");
                }
            };
            if (isLoadingSynchronously) {
                synchronized (mDeferredBindRunnables) {
                    mDeferredBindRunnables.add(r);
                }
            } else {
                runOnMainThread(r);
            }
        }

        /**
         * Refreshes the shortcuts shown on the workspace.
         */
        private void startBinding(final LauncherContract.View oldView) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    LauncherContract.View view = tryGetView(oldView);
                    if (view != null) {
                        setWorkspaceLoading(true);
                        // If we're starting binding all over again, clear any bind calls we'd postponed in
                        // the past (see waitUntilResume) -- we don't need them since we're starting binding
                        // from scratch again
                        mBindOnResumeCallbacks.clear();
                        //
                        view.startBinding();
                    }
                }
            });
        }


        /**
         * Unbinds all the sBgWorkspaceItems and sBgAppWidgets on the main thread
         */
        private void unbindWorkspaceItemsOnMainThread() {
            // Ensure that we don't use the same workspace items data structure on the main thread
            // by making a copy of workspace items first.
            // 确保不在主线程中使用同一个workspace item 数据源, 首先拷贝一份数据
            final ArrayList<ItemInfo> tmpItems = new ArrayList<>();
            synchronized (sBgLock) {
                tmpItems.addAll(sBgWorkspaceItems);
//                tmpItems.addAll(sBgAppWidgets);
            }
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    for (ItemInfo item : tmpItems) {
                        item.unbind();
                    }
                }
            });
        }

        /**
         * Filters the set of items who are directly or indirectly (via another container) on the
         * specified screen.
         * 在指定屏幕中的项目集中筛选，谁是直接的,谁是间接的(经由其他容器)
         */
        private void filterCurrentWorkspaceItems(long currentScreenId,
                                                 ArrayList<ItemInfo> allWorkspaceItems,
                                                 ArrayList<ItemInfo> currentScreenItems,
                                                 ArrayList<ItemInfo> otherScreenItems) {
            // Purge any null ItemInfos
            // 清除所有的null项
            Iterator<ItemInfo> iterator = allWorkspaceItems.iterator();
            while (iterator.hasNext()) {
                ItemInfo i = iterator.next();
                if (i == null) {
                    iterator.remove();
                }
            }
            //
            Set<Long> itemsOnScreen = new HashSet<>();
            Collections.sort(allWorkspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    // 从小到大排序
                    return (int) (lhs.container - rhs.container);
                }
            });
            // 是HotSeat 或者screenId = currentScreenId, 表示属于当前
            for (ItemInfo info : allWorkspaceItems) {
                if (info.container == LauncherSettings.ItemColumns.CONTAINER_DESKTOP) {
                    if (info.screenId == currentScreenId) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                } else if (info.container == LauncherSettings.ItemColumns.CONTAINER_HOT_SEAT) {
                    currentScreenItems.add(info);
                    itemsOnScreen.add(info.id);
                } else {
                    if (itemsOnScreen.contains(info.container)) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                }
            }

        }

        /**
         * Filters the set of widgets which are on the specified screen.
         */
//        private void filterCurrentAppWidgets(long currentScreenId,
//                                             ArrayList<LauncherAppWidgetInfo> appWidgets,
//                                             ArrayList<LauncherAppWidgetInfo> currentScreenWidgets,
//                                             ArrayList<LauncherAppWidgetInfo> otherScreenWidgets) {
//
//            for (LauncherAppWidgetInfo widget : appWidgets) {
//                if (widget == null) continue;
//                if (widget.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
//                        widget.screenId == currentScreenId) {
//                    currentScreenWidgets.add(widget);
//                } else {
//                    otherScreenWidgets.add(widget);
//                }
//            }
//        }

        /**
         * Filters the set of folders which are on the specified screen.
         */
        private void filterCurrentFolders(long currentScreenId,
                                          LongArrayMap<ItemInfo> itemsIdMap,
                                          LongArrayMap<FolderInfo> folders,
                                          LongArrayMap<FolderInfo> currentScreenFolders,
                                          LongArrayMap<FolderInfo> otherScreenFolders) {

            int total = folders.size();
            for (int i = 0; i < total; i++) {
                long id = folders.keyAt(i);
                FolderInfo folder = folders.valueAt(i);
                ItemInfo info = itemsIdMap.get(id);
                if (info == null || folder == null) {
                    continue;
                }
                if (info.container == LauncherSettings.ItemColumns.CONTAINER_DESKTOP &&
                        info.screenId == currentScreenId) {
                    currentScreenFolders.put(id, folder);
                } else {
                    otherScreenFolders.put(id, folder);
                }
            }
        }

        /**
         * Sorts the set of items by hotseat, workspace (spatially from top to bottom, left to right)
         * 对hotSeat workspace进行排序, 空间上从上到下，从左到右
         */
        private void sortWorkspaceItemsSpatially(ArrayList<ItemInfo> workspaceItems) {
            final InvariantDeviceProfile profile = LauncherAppState.getInstance().getInvariantDeviceProfile();
            // XXX: review this
            Collections.sort(workspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    int cellCountX = profile.numColumns;
                    int cellCountY = profile.numRows;
                    int screenOffset = cellCountX * cellCountY;
                    // +1 hotseat
                    int containerOffset = screenOffset * (Workspace.SCREEN_COUNT + 1);
                    // ItemInfo.container * containerOffset  同类容器的起点值(hotseat, workspace)
                    // ItemInfo.screenId * screenOffset 屏幕页的起点值
                    // ItemInfo.cellY * cellCountX + ItemInfo.cellX对应屏幕中位置
                    long lr = (lhs.container * containerOffset + lhs.screenId * screenOffset +
                            lhs.cellY * cellCountX + lhs.cellX);
                    long rr = (rhs.container * containerOffset + rhs.screenId * screenOffset +
                            rhs.cellY * cellCountX + rhs.cellX);

                    return (int) (lr - rr);
                }
            });
        }

        private void loadAndBindAllApps() {

        }

        void runBindSynchronousPage(int synchronousBindPage) {

        }


        private LauncherContract.View tryGetView(LauncherContract.View oldView) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }
                if (mView == null) {
                    return null;
                }
                final LauncherContract.View view = mView;
                if (view != oldView) {
                    return null;
                }
                return view;
            }
        }

        private void bindWorkspaceScreens(final LauncherContract.View oldView, final List<Long> orderedScreens) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    LauncherContract.View callbacks = tryGetView(oldView);
                    if (callbacks != null) {
                        callbacks.bindScreens(orderedScreens);
                    }
                }
            });
        }

        /**
         * 加载items到工作区
         *
         * @param oldView
         * @param workspaceItems
         * @param appWidgets
         * @param folders
         * @param deferredBindRunnables null 表示同步加载, != null 放入该延迟加载线程中
         */
        private void bindWorkspaceItems(final LauncherContract.View oldView,
                                        @NonNull final List<ItemInfo> workspaceItems,
                                        @NonNull final List<LauncherAppWidgetInfo> appWidgets,
                                        @NonNull final LongArrayMap<FolderInfo> folders,
                                        List<Runnable> deferredBindRunnables) {
            final boolean postOnMainThread = (deferredBindRunnables != null);
            // Bind the workspace items
            // 绑定items
            int num = workspaceItems.size();
            ZLog.i(LogTag.TAG_LOADER, "开始绑定工作区item : " + num);
            for (int i = 0; i < num; i += ITEMS_CHUNK) {
                final int start = i;
                final int chunkSize = (i + ITEMS_CHUNK <= num) ? ITEMS_CHUNK : (num - i);
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        LauncherContract.View view = tryGetView(oldView);
                        if (view != null) {
                            view.bindItems(workspaceItems, start, start + chunkSize, false);
                        }
                    }
                };
                if (postOnMainThread) {
                    synchronized (deferredBindRunnables) {
                        deferredBindRunnables.add(r);
                    }
                } else {
                    runOnMainThread(r);
                }
            }
            // Bind the folders
            // 绑定文件夹
            if (!folders.isEmpty()) {

            }
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }
    }

}
