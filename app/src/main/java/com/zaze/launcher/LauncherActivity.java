package com.zaze.launcher;

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
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;

import com.zaze.launcher.data.entity.ItemInfo;
import com.zaze.launcher.databinding.ActivityLauncherBinding;
import com.zaze.launcher.util.LogTag;
import com.zaze.launcher.util.Utilities;
import com.zaze.launcher.view.HotSeat;
import com.zaze.launcher.view.PagedView;
import com.zaze.launcher.view.Workspace;
import com.zaze.utils.log.ZLog;
import com.zaze.utils.log.ZTag;

import java.util.List;

/**
 * @author zaze
 */
public class LauncherActivity extends AppCompatActivity implements LauncherContract.View {

    private LauncherPresenter mViewModel;
    private LauncherCallbacks mLauncherCallbacks;
    private HotSeat mHotSeat;
    private Workspace mWorkspace;

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
        ZLog.i(LogTag.TAG_DEBUG, "onAttachedToWindow");
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
        ZLog.i(LogTag.TAG_DEBUG, "onDetachedFromWindow");
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
        ZLog.i(ZTag.TAG_DEBUG, "加载之前保存的状态");
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

    @Override
    public void startBinding() {

    }

    @Override
    public void bindScreens(List<Long> orderedScreens) {

    }

    @Override
    public void onPageBoundSynchronously(int currentScreen) {

    }

    @Override
    public void bindItems(List<ItemInfo> items, int start, int end, boolean forceAnimateIcons) {

    }
}
