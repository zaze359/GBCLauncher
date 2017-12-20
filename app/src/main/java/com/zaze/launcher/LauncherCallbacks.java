package com.zaze.launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * LauncherCallbacks is an interface used to extend the Launcher activity. It includes many hooks
 * in order to add additional functionality. Some of these are very general, and give extending
 * classes the ability to react to Activity life-cycle or specific user interactions. Others
 * are more specific and relate to replacing parts of the application, for example, the search
 * interface or the wallpaper picker.
 */
public interface LauncherCallbacks {

    /**
     * Activity life-cycle methods. These methods are triggered after
     * the code in the corresponding Launcher method is executed.
     */
    void preOnCreate();

    void onCreate(Bundle savedInstanceState);

    void preOnResume();

    void onResume();

    void onStart();

    void onStop();

    void onPause();

    void onDestroy();

    void onSaveInstanceState(Bundle outState);

    void onPostCreate(Bundle savedInstanceState);

    void onNewIntent(Intent intent);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    int[] grantResults);

    void onWindowFocusChanged(boolean hasFocus);

    boolean onPrepareOptionsMenu(Menu menu);

    void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args);

    void onHomeIntent();

    boolean handleBackPressed();

    void onTrimMemory(int level);

    /**
     * Extension points for providing custom behavior on certain user interactions.
     */
    void onLauncherProviderChange();

    void finishBindingItems(final boolean upgradePath);

    void onClickAllAppsButton(View v);

//    void bindAllApplications(ArrayList<AppInfo> apps);

    void onClickFolderIcon(View v);

    void onClickAppShortcut(View v);

    @Deprecated
    void onClickPagedViewIcon(View v);

    void onClickWallpaperPicker(View v);

    void onClickSettingsButton(View v);

    void onClickAddWidgetButton(View v);

    void onPageSwitch(View newPage, int newPageIndex);

    void onWorkspaceLockedChanged();

    void onDragStarted(View view);

    void onInteractionBegin();

    void onInteractionEnd();

    /**
     * Extension points for replacing the search experience
     */
    @Deprecated
    boolean forceDisableVoiceButtonProxy();

    boolean providesSearch();

    boolean startSearch(String initialQuery, boolean selectInitialQuery,
                        Bundle appSearchData, Rect sourceBounds);

    boolean startSearchFromAllApps(String query);

    @Deprecated
    void startVoice();

    boolean hasCustomContentToLeft();

    void populateCustomContentContainer();

    View getQsbBar();

    /**
     * Extensions points for adding / replacing some other aspects of the Launcher experience.
     * 扩展方式有 添加或者替换 启动器的某些方面
     */
    Intent getFirstRunActivity();

    /**
     * To be overridden by subclasses to indicate that there is an activity to launch
     * before showing the standard launcher experience.
     * 子类重写，表示是否有一个activity需要显示，在展示标准的launcher之前
     * return boolean
     */
    boolean hasFirstRunActivity();

    boolean hasDismissableIntroScreen();

    View getIntroScreen();

    boolean shouldMoveToDefaultScreenOnHomeIntent();

    boolean hasSettings();

    @Deprecated
    ComponentName getWallpaperPickerComponent();

    boolean overrideWallpaperDimensions();

    boolean isLauncherPreinstalled();

//    AllAppsSearchBarController getAllAppsSearchBarController();

//    List<ComponentKey> getPredictedApps();

    /**
     * Returning true will immediately result in a call to {@link #setLauncherOverlayView(ViewGroup,
     * com.android.launcher3.Launcher.LauncherOverlayCallbacks)}.
     * 是否有覆盖层
     *
     * @return true if this launcher extension will provide an overlay
     */
    boolean hasLauncherOverlay();

    /**
     * Handshake to establish an overlay relationship
     *
     * @param container Full screen overlay ViewGroup into which custom views can be placed.
     * @param callbacks A set of callbacks provided by Launcher in relation to the overlay
     * @return an interface used to make requests and notify the Launcher in relation to the overlay
     */
//    Launcher.LauncherOverlay setLauncherOverlayView(InsettableFrameLayout container,
//                                                           Launcher.LauncherOverlayCallbacks callbacks);

    /**
     * Sets the callbacks to allow reacting the actions of search overlays of the launcher.
     *
     * @param callbacks A set of callbacks to the Launcher, is actually a LauncherSearchCallback,
     *                  but for implementation purposes is passed around as an object.
     */
    void setLauncherSearchCallback(Object callbacks);
}
