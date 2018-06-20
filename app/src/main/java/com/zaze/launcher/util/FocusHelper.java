package com.zaze.launcher.util;

import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;

import com.zaze.launcher.DeviceProfile;
import com.zaze.launcher.LauncherAppState;
import com.zaze.launcher.R;
import com.zaze.launcher.view.CellLayout;
import com.zaze.launcher.view.HotSeat;
import com.zaze.launcher.view.ShortcutAndWidgetContainer;
import com.zaze.launcher.view.Workspace;
import com.zaze.utils.log.ZLog;

/**
 * Description : 焦点处理类
 *
 * @author : ZAZE
 * @version : 2018-06-09 - 16:04
 */
public class FocusHelper {
    private static final String TAG = "FocusHelper";

    static boolean handleIconKeyEvent(View v, int keyCode, KeyEvent e) {
        boolean consume = FocusLogic.shouldConsume(keyCode);
        if (e.getAction() == KeyEvent.ACTION_UP || !consume) {
            return consume;
        }
        return consume;
    }


    /**
     * Handles key events in the workspace hot seat (bottom of the screen).
     * <p>Currently we don't special case for the phone UI in different orientations, even though
     * the hotseat is on the side in landscape mode. This is to ensure that accessibility
     * consistency is maintained across rotations.
     */
    static boolean handleHotSeatButtonKeyEvent(View v, int keyCode, KeyEvent e) {
        boolean consume = FocusLogic.shouldConsume(keyCode);
        if (e.getAction() == KeyEvent.ACTION_UP || !consume) {
            return consume;
        }
        DeviceProfile profile = LauncherAppState.getInstance().getDeviceProfile();
        ZLog.v(TAG, String.format(
                "Handle HOTSEAT BUTTONS keyevent=[%s] on hotseat buttons, isVertical=%s",
                KeyEvent.keyCodeToString(keyCode), profile.isVerticalBarLayout()));

        // Initialize the variables.
        final ShortcutAndWidgetContainer hotSeatParent = (ShortcutAndWidgetContainer) v.getParent();
        final CellLayout hotSeatLayout = (CellLayout) hotSeatParent.getParent();
        HotSeat hotSeat = (HotSeat) hotSeatLayout.getParent();


        int countX = -1;
        int countY = -1;
        int iconIndex = hotSeatParent.indexOfChild(v);
        int iconRank = ((CellLayout.LayoutParams) hotSeatLayout.getShortcutsAndWidgets()
                .getChildAt(iconIndex).getLayoutParams()).cellX;
        Workspace workspace = v.getRootView().findViewById(R.id.launcher_workspace);
        int pageIndex = workspace.getNextPage();
        int pageCount = workspace.getChildCount();
        final CellLayout iconLayout = (CellLayout) workspace.getChildAt(pageIndex);
        if (iconLayout == null) {
            // This check is to guard against cases where key strokes rushes in when workspace
            // child creation/deletion is still in flux. (e.g., during drop or fling
            // animation.)
            // 这个检查是为了防止当工作区子创建/删除仍然在变化的时候，输入键的情况。
            // (e.g., during drop or fling animation.)
            return consume;
        }
        final ViewGroup iconParent = iconLayout.getShortcutsAndWidgets();
        ViewGroup parent = null;

        // UI 矩阵初始化
        int[][] matrix = null;
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP &&
                !profile.isVerticalBarLayout()) {
            matrix = FocusLogic.createSparseMatrix(iconLayout, hotSeatLayout,
                    true /* hotseat horizontal */, profile.inv.hotSeatAllAppsRank,
                    iconRank == profile.inv.hotSeatAllAppsRank /* include all apps icon */);
            iconIndex += iconParent.getChildCount();
            countX = iconLayout.getCountX();
            countY = iconLayout.getCountY() + hotSeatLayout.getCountY();
            parent = iconParent;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT &&
                profile.isVerticalBarLayout()) {
            matrix = FocusLogic.createSparseMatrix(iconLayout, hotSeatLayout,
                    false /* hotseat horizontal */, profile.inv.hotSeatAllAppsRank,
                    iconRank == profile.inv.hotSeatAllAppsRank /* include all apps icon */);
            iconIndex += iconParent.getChildCount();
            countX = iconLayout.getCountX() + hotSeatLayout.getCountX();
            countY = iconLayout.getCountY();
            parent = iconParent;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT &&
                profile.isVerticalBarLayout()) {
            keyCode = KeyEvent.KEYCODE_PAGE_DOWN;
        } else {
            // For other KEYCODE_DPAD_LEFT and KEYCODE_DPAD_RIGHT navigation, do not use the
            // matrix extended with hotseat.
            matrix = FocusLogic.createSparseMatrix(hotSeatLayout);
            countX = hotSeatLayout.getCountX();
            countY = hotSeatLayout.getCountY();
            parent = hotSeatParent;
        }

        // Process the focus.
        int newIconIndex = FocusLogic.handleKeyEvent(keyCode, countX,
                countY, matrix, iconIndex, pageIndex, pageCount, Utilities.isRtl(v.getResources()));

        View newIcon = null;
        if (newIconIndex == FocusLogic.NEXT_PAGE_FIRST_ITEM) {
            parent = getCellLayoutChildrenForIndex(workspace, pageIndex + 1);
            newIcon = parent.getChildAt(0);
            // TODO(hyunyoungs): handle cases where the child is not an icon but
            // a folder or a widget.
            workspace.snapToPage(pageIndex + 1);
        }
        if (parent == iconParent && newIconIndex >= iconParent.getChildCount()) {
            newIconIndex -= iconParent.getChildCount();
        }
        if (parent != null) {
            if (newIcon == null && newIconIndex >= 0) {
                newIcon = parent.getChildAt(newIconIndex);
            }
            if (newIcon != null) {
                newIcon.requestFocus();
                playSoundEffect(keyCode, v);
            }
        }
        return consume;
    }

    static void playSoundEffect(int keyCode, View v) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                v.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                v.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_PAGE_DOWN:
            case KeyEvent.KEYCODE_MOVE_END:
                v.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_PAGE_UP:
            case KeyEvent.KEYCODE_MOVE_HOME:
                v.playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
                break;
            default:
                break;
        }
    }

    // --------------------------------------------------
    // --------------------------------------------------

    /**
     * A keyboard listener we set on all the workspace icons.
     */
    public static class IconKeyEventListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return FocusHelper.handleIconKeyEvent(v, keyCode, event);
        }
    }

    /**
     * A keyboard listener we set on all the hotseat buttons.
     */
    public static class HotSeatIconKeyEventListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return FocusHelper.handleHotSeatButtonKeyEvent(v, keyCode, event);
        }
    }


    /**
     * Private helper method to get the CellLayoutChildren given a CellLayout index.
     */
    private static ShortcutAndWidgetContainer getCellLayoutChildrenForIndex(
            ViewGroup container, int i) {
        CellLayout parent = (CellLayout) container.getChildAt(i);
        return parent.getShortcutsAndWidgets();
    }


}

