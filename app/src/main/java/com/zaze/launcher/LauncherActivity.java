package com.zaze.launcher;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;

import com.zaze.launcher.data.entity.FolderInfo;
import com.zaze.launcher.data.entity.ItemInfo;
import com.zaze.launcher.data.entity.ShortcutInfo;
import com.zaze.launcher.databinding.ActivityLauncherBinding;
import com.zaze.launcher.util.LauncherAnimUtils;
import com.zaze.launcher.util.LogTag;
import com.zaze.launcher.util.Utilities;
import com.zaze.launcher.view.BubbleTextView;
import com.zaze.launcher.view.CellLayout;
import com.zaze.launcher.view.FolderIcon;
import com.zaze.launcher.view.HotSeat;
import com.zaze.launcher.view.PagedView;
import com.zaze.launcher.view.Workspace;
import com.zaze.utils.log.ZLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zaze
 */
public class LauncherActivity extends AppCompatActivity implements LauncherContract.View, View.OnClickListener {
    private static final String TAG = "LauncherActivity" + LogTag.TAG_BASE;

    private LauncherPresenter mViewModel;
    private LauncherCallbacks mLauncherCallbacks;
    private HotSeat mHotSeat;
    private Workspace mWorkspace;
    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<>();
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<>();
    private LayoutInflater mInflater;


    public void setLauncherCallbacks(LauncherCallbacks launcherCallbacks) {
        this.mLauncherCallbacks = launcherCallbacks;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mViewModel.onPostCreate(savedInstanceState);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZLog.i(TAG, "onAttachedToWindow");
        setupTransparentSystemBarsForLollipop();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupTransparentSystemBarsForLollipop() {
        if (Utilities.ATLEAST_LOLLIPOP) {
            Window window = getWindow();
            window.getAttributes().systemUiVisibility |=
                    (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZLog.i(TAG, "onDetachedFromWindow");
//        updateAutoAdvanceState
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: 2017/12/19 DEBUG_STRICT_MODE
        mViewModel = findOrCreateViewModel();
        mViewModel.setLauncherCallbacks(mLauncherCallbacks);
        mViewModel.preOnCreate();
        super.onCreate(savedInstanceState);
        mInflater = getLayoutInflater();

        ActivityLauncherBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_launcher);
        binding.setViewModel(mViewModel);
        // --------------------------------------------------
        mViewModel.initialize(this);
        setupPermission();
        setupViews();
//        lockAllApps();
        restoreState(savedInstanceState);
        mViewModel.startLoader(PagedView.INVALID_RESTORE_PAGE);
        setOrientation();
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onCreate(savedInstanceState);
            if (mLauncherCallbacks.hasLauncherOverlay()) {
                ViewStub stub = findViewById(R.id.launcher_overlay_stub);
                View view = stub.inflate();
            }
        }
        if (shouldShowIntroScreen()) {
            showIntroScreen();
        } else {
            showFirstRunActivity();
            mViewModel.showFirstRunClings();
        }
    }

    /**
     * 申请权限
     */
    private boolean setupPermission() {
        boolean bool = true;
//        bool = Utilities.checkAndRequestUserPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionCode.WRITE_EXTERNAL_STORAGE);
//        Utilities.checkAndRequestUserPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, PermissionCode.READ_EXTERNAL_STORAGE);
//        Utilities.checkAndRequestUserPermission(this, Manifest.permission.CALL_PHONE, PermissionCode.REQUEST_PERMISSION_CALL_PHONE);
        return bool;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (!isGranted) {
            finish();
        }
    }

    /**
     * 构建ViewModel
     *
     * @return LauncherPresenter
     */
    private LauncherPresenter findOrCreateViewModel() {
        return new LauncherPresenter(this);
    }

    /**
     * 初始化界面
     */
    private void setupViews() {
        mHotSeat = findViewById(R.id.launcher_hot_seat);
        mHotSeat.layout();
        mWorkspace = findViewById(R.id.launcher_workspace);
//        mWorkspace.setPageSwitchListener(this);
    }

    /**
     * 加载之前保存的状态
     *
     * @param savedInstanceState
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        ZLog.i(TAG, "加载之前保存的状态");
        // TODO: 2018/3/6


    }

    /**
     * 设置屏幕方向
     */
    private void setOrientation() {
        // 忽略物理感应器——即显示方向与物理感应器无关
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    /**
     * To be overriden by subclasses to indicate whether the in-activity intro screen has been
     * dismissed. This method is ignored if #hasDismissableIntroScreen returns false.
     */
    private boolean shouldShowIntroScreen() {
//        return hasDismissableIntroScreen() && !mSharedPrefs.getBoolean(INTRO_SCREEN_DISMISSED, false);
        return false;
    }

    protected void showIntroScreen() {
//        Log.i(TAG, "showIntroScreen");
//        View introScreen = getIntroScreen();
//        changeWallpaperVisiblity(false);
//        if (introScreen != null) {
//            mDragLayer.showOverlayView(introScreen);
//        }
//        if (mLauncherOverlayContainer != null) {
//            mLauncherOverlayContainer.setVisibility(View.INVISIBLE);
//        }
    }


    private void showFirstRunActivity() {
        if (mViewModel.shouldRunFirstRunActivity() && mViewModel.hasFirstRunActivity()) {
            Intent intent = mViewModel.getFirstRunActivity();
            if (intent != null) {
                startActivity(intent);
                mViewModel.markFirstRunActivityShown();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewModel.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViewModel.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.onStart();
    }

    @Override
    protected void onResume() {
        mViewModel.preOnResume();
        super.onResume();
        mViewModel.onResume();
        mHotSeat.resetLayout();
    }

    private void setWorkspaceBackground(int background) {
//        switch (background) {
//            case WORKSPACE_BACKGROUND_TRANSPARENT:
//                getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                break;
//            case WORKSPACE_BACKGROUND_BLACK:
//                getWindow().setBackgroundDrawable(null);
//                break;
//            default:
//                getWindow().setBackgroundDrawable(mWorkspaceBackgroundDrawable);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewModel.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    // 音量减小键
                    // TODO: 2017/12/19
//                    if (Utilities.isPropertyEnabled(DUMP_STATE_PROPERTY)) {
//                        dumpState();
//                        return true;
//                    }
                    break;
                default:
                    break;
            }
        } else if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mViewModel.handleBackPressed()) {
            // TODO: 2017/12/19
        }
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mViewModel.onTrimMemory(level);
    }

    // --------------------------------------------------

    @Override
    public void onClick(View v) {

    }
    // --------------------------------------------------


    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return Workspace.SCREEN_COUNT / 2;
        }
    }

    // ------------------------ about bind start --------------------------

    /**
     * Refreshes the shortcuts shown on the workspace.
     * 刷新工作区的快捷图标
     */
    @Override
    public void startBinding() {
        ZLog.i(TAG, "startBinding");
        // Clear the workspace because it's going to be rebound
        mWorkspace.clearDropTargets();
        mWorkspace.removeAllWorkspaceScreens();
//        mWidgetsToAdvance.clear();
        if (mHotSeat != null) {
            mHotSeat.resetLayout();
        }
    }

    @Override
    public void bindScreens(List<Long> orderedScreenIds) {
        ZLog.i(TAG, "bindScreens");
        bindAddScreens(orderedScreenIds);
        // If there are no screens, we need to have an empty screen
        if (orderedScreenIds.size() == 0) {
            mWorkspace.addExtraEmptyScreen();
        }
        // Create the custom content page (this call updates mDefaultScreen which calls
        // setCurrentPage() so ensure that all pages are added before calling this).
        if (mViewModel.hasCustomContentToLeft()) {
            mWorkspace.createCustomContentContainer();
            mViewModel.populateCustomContentContainer();
        }
    }

    private void bindAddScreens(List<Long> orderedScreenIds) {
        int count = orderedScreenIds.size();
        for (int i = 0; i < count; i++) {
            mWorkspace.insertNewWorkspaceScreenBeforeEmptyScreen(orderedScreenIds.get(i));
        }
    }

    @Override
    public void onPageBoundSynchronously(int page) {
        ZLog.i(TAG, "onPageBoundSynchronously : " + page);
        mSynchronouslyBoundPages.add(page);
    }

    @Override
    public void bindItems(final List<ItemInfo> shortcuts, final int start, final int end, final boolean forceAnimateIcons) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                bindItems(shortcuts, start, end, forceAnimateIcons);
            }
        };
        if (mViewModel.waitUntilResume(r)) {
            return;
        }
        ZLog.i(TAG, "bindItems : %s/%s", start, end);
        // Get the list of added shortcuts and intersect them with the set of shortcuts here
        final AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        final Collection<Animator> bounceAnims = new ArrayList<>();
//        final boolean animateIcons = forceAnimateIcons && canRunNewAppsAnimation();
        final boolean animateIcons = false;
        Workspace workspace = mWorkspace;
        long newShortcutsScreenId = -1;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.ItemColumns.CONTAINER_HOT_SEAT &&
                    mHotSeat == null) {
                continue;
            }
            final View view;
            switch (item.itemType) {
                case LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION:
                case LauncherSettings.ItemColumns.ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    view = createShortcut(info);
                    /*
                     * TODO: FIX collision case
                     */
                    if (item.container == LauncherSettings.ItemColumns.CONTAINER_DESKTOP) {
                        CellLayout cl = mWorkspace.getScreenWithId(item.screenId);
                        if (cl != null && cl.isOccupied(item.cellX, item.cellY)) {
                            View v = cl.getChildAt(item.cellX, item.cellY);
                            Object tag = v.getTag();
                            ZLog.d(TAG, "Collision while binding workspace item: " + item
                                    + ". Collides with " + tag);
                        }
                    }
                    break;
                case LauncherSettings.ItemColumns.ITEM_TYPE_FOLDER:
                    // TODO: 2018/3/16
                    view = FolderIcon.fromXml(R.layout.folder_icon_view, this,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FolderInfo) item, null);
                    break;
                default:
                    throw new RuntimeException("Invalid Item Type");
            }
            workspace.addInScreenFromBind(view, item.container, item.screenId, item.cellX,
                    item.cellY, 1, 1);
            if (animateIcons) {
                // Animate all the applications up now
                // TODO: 2018/3/16
            }
        }

        if (animateIcons) {
            // TODO: 2018/3/16
        }
        workspace.requestLayout();
    }

    @Override
    public void finishBindingItems() {
        ZLog.i(TAG, "finishBindingItems");
    }

    // ----------------------- about bind end   --------------------------


    public HotSeat getHotSeat() {
        return mHotSeat;
    }

    public int getViewIdForItem(ItemInfo info) {
        // This cast is safe given the > 2B range for int.
        int itemId = (int) info.id;
        if (mItemIdToViewId.containsKey(itemId)) {
            return mItemIdToViewId.get(itemId);
        }
        int viewId = generateViewId();
        mItemIdToViewId.put(itemId, viewId);
        return viewId;
    }

    public static int generateViewId() {
        if (Utilities.ATLEAST_JB_MR1) {
            return View.generateViewId();
        } else {
            // View.generateViewId() is not available. The following fallback logic is a copy
            // of its implementation.
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) {
                    // Roll over to 1, not 0.
                    newValue = 1;
                }
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }
    // --------------------------------------------------

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut((ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param parent The group the shortcut belongs to.
     * @param info   The data structure describing the shortcut.
     * @return A View inflated from layoutResId.
     */
    public View createShortcut(ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(R.layout.app_icon_view,
                parent, false);
        favorite.applyFromShortcutInfo(info, LauncherAppState.getInstance().getIconCache());
        favorite.setCompoundDrawablePadding(
                LauncherAppState.getInstance().getDeviceProfile().iconDrawablePaddingPx
        );
        favorite.setOnClickListener(this);
//        favorite.setOnFocusChangeListener(mFocusHandler);
        return favorite;
    }
}
