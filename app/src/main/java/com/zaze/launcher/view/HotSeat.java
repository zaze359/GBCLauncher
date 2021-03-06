package com.zaze.launcher.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zaze.launcher.DeviceProfile;
import com.zaze.launcher.LauncherAppState;
import com.zaze.launcher.R;
import com.zaze.launcher.util.Utilities;

/**
 * Description : 热键
 *
 * @author : ZAZE
 * @version : 2018-01-07 - 00:00
 */
public class HotSeat extends FrameLayout {
    private CellLayout mContent;

    private final boolean mHasVerticalHotSeat;
    private int mAllAppsButtonRank;


    public HotSeat(@NonNull Context context) {
        this(context, null);
    }

    public HotSeat(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HotSeat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHasVerticalHotSeat = LauncherAppState.getInstance().getDeviceProfile().isVerticalBarLayout();
    }


    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        mContent.setOnLongClickListener(l);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        DeviceProfile grid = LauncherAppState.getInstance().getDeviceProfile();
        int hotSeatCount = grid.inv.numHotSeatIcons;
        mContent = findViewById(R.id.hot_seat_layout);
        mAllAppsButtonRank = grid.inv.hotSeatAllAppsRank;
        if (grid.isLandscape && !grid.isLargeTablet) {
            mContent.setGridSize(1, hotSeatCount);
        } else {
            mContent.setGridSize(hotSeatCount, 1);
        }
        mContent.setIsHotSeat(true);
        resetLayout();
    }

    public void resetLayout() {
        mContent.removeAllViewsInLayout();
        // 默认添加 allAppsButton
        Context context = getContext();
        DeviceProfile grid = LauncherAppState.getInstance().getDeviceProfile();
        LayoutInflater inflater = LayoutInflater.from(context);
        TextView allAppsButton = (TextView) inflater.inflate(R.layout.all_apps_button, mContent, false);
        Drawable d = context.getResources().getDrawable(R.drawable.all_apps_button_icon);
        grid.resizeIconDrawable(d);
        allAppsButton.setCompoundDrawables(null, d, null, null);
        allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
        allAppsButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        allAppsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        allAppsButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        allAppsButton.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
        // Note: We do this to ensure that the hotseat is always laid out in the orientation of
        // the hotseat in order regardless of which orientation they were added
        int x = getCellXFromOrder(mAllAppsButtonRank);
        int y = getCellYFromOrder(mAllAppsButtonRank);
        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x, y, 1, 1);
        lp.canReorder = false;
        mContent.addViewToCellLayout(allAppsButton, -1, allAppsButton.getId(), lp, true);
    }

    /**
     * 处理layoutParams
     */
    public void layout() {
        FrameLayout.LayoutParams hotSeatLp = (LayoutParams) getLayoutParams();
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDeviceProfile();
        boolean isLayoutRtl = Utilities.isRtl(getResources());
        Rect padding = deviceProfile.getWorkspacePadding(isLayoutRtl);
        int edgeMarginPx = deviceProfile.edgeMarginPx;
        int hotSeatBarHeightPx = deviceProfile.hotSeatBarHeightPx;
        if (deviceProfile.isVerticalBarLayout()) {
            // 垂直时靠右
            hotSeatLp.gravity = Gravity.END;
            hotSeatLp.width = hotSeatBarHeightPx;
            hotSeatLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mContent.setPadding(0, 2 * edgeMarginPx, 0, 2 * edgeMarginPx);
        } else if (deviceProfile.isTablet) {
            // 平板 固定底部, padding特殊处理
            hotSeatLp.gravity = Gravity.BOTTOM;
            hotSeatLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            hotSeatLp.height = hotSeatBarHeightPx;
            setPadding(edgeMarginPx + padding.left, 0,
                    edgeMarginPx + padding.right,
                    2 * edgeMarginPx);
        } else {
            // For phones, layout the hotseat witho
            hotSeatLp.gravity = Gravity.BOTTOM;
            hotSeatLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            hotSeatLp.height = hotSeatBarHeightPx;
            mContent.setPadding(2 * edgeMarginPx, 0,
                    2 * edgeMarginPx, 0);
        }
        setLayoutParams(hotSeatLp);
    }

    public CellLayout getLayout() {
        return mContent;
    }


    // --------------------------------------------------

    /**
     * 在X轴上的顺序
     *
     * @param rank rank
     * @return
     */
    int getCellXFromOrder(int rank) {
        return mHasVerticalHotSeat ? 0 : rank;
    }

    /**
     * 在Y轴上的顺序
     *
     * @param rank rank
     * @return
     */
    int getCellYFromOrder(int rank) {
        return mHasVerticalHotSeat ? (mContent.getCountY() - (rank + 1)) : 0;
    }

    int getOrderInHotSeat(int x, int y) {
        return mHasVerticalHotSeat ? (mContent.getCountY() - y - 1) : x;
    }
}
