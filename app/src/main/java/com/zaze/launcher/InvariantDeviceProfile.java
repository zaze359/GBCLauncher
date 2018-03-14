/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaze.launcher;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.zaze.launcher.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 一些不变的配置
 */
public class InvariantDeviceProfile {

    /**
     * 包含状态栏 不包含状态栏
     * 比如(768, 744)
     */
    private static Point smallestSize = null;
    /**
     * 包含状态栏 不包含状态栏
     * 比如(1024, 1000)
     */
    private static Point largestSize = null;
    /**
     * (768, 1024)
     */
    private static Point realSize = null;
    private static float DEFAULT_ICON_SIZE_DP = 60;
    private static final float ICON_SIZE_DEFINED_IN_APP_DP = 48;

    // --------------------------------------------------
    // Constants that affects the interpolation curve between statically defined device profile
    // buckets.
    private static float KNEARESTNEIGHBOR = 3;
    private static float WEIGHT_POWER = 5;

    // used to offset float not being able to express extremely small weights in extreme cases.
    private static float WEIGHT_EFFICIENT = 100000f;

    // --------------------------------------------------

    // Profile-defining invariant properties
    String name;
    float minWidthDps;
    float minHeightDps;
    /**
     * Number of icons per row and column in the workspace.
     * 工作区中每行的图标个数
     */
    public int numRows;
    /**
     * Number of icons per row and column in the workspace.
     * 工作区中每列的图标个数
     */
    public int numColumns;
    /**
     * The minimum number of predicted apps in all apps.
     */
    int minAllAppsPredictionColumns;
    /**
     * Number of icons per row and column in the folder.
     */
    public int numFolderRows;
    public int numFolderColumns;
    float iconSize;
    float iconTextSize;

    /**
     * Number of icons inside the hotseat area.
     */
    public int numHotSeatIcons;
    public float hotSeatIconSize;

    /**
     * hotSeat的位置(排行)
     */
    public int hotSeatAllAppsRank;

    int defaultLayoutId;

    public int iconBitmapSize;
    int fillResIconDpi;


    InvariantDeviceProfile() {
    }

    /**
     * @param n     name
     * @param w     minWidthDps
     * @param h     minHeightDps
     * @param r     numRows
     * @param c     numColumns
     * @param fr    numFolderRows
     * @param fc    numFolderColumns
     * @param maapc minAllAppsPredictionColumns
     * @param is    iconSize
     * @param its   iconTextSize
     * @param hs    numHotSeatIcons
     * @param his   hotSeatIconSize
     * @param dlId  defaultLayoutId
     */
    InvariantDeviceProfile(String n, float w, float h, int r, int c, int fr, int fc, int maapc,
                           float is, float its, int hs, float his, int dlId) {
        // Ensure that we have an odd number of hot seat items (since we need to place all apps)
        if (hs % 2 == 0) {
            throw new RuntimeException("All Device Profiles must have an odd number of hotseat spaces");
        }
        name = n;
        minWidthDps = w;
        minHeightDps = h;
        numRows = r;
        numColumns = c;
        numFolderRows = fr;
        numFolderColumns = fc;
        minAllAppsPredictionColumns = maapc;
        iconSize = is;
        iconTextSize = its;
        numHotSeatIcons = hs;
        hotSeatIconSize = his;
        defaultLayoutId = dlId;
    }

    public InvariantDeviceProfile(InvariantDeviceProfile p) {
        this(p.name, p.minWidthDps, p.minHeightDps, p.numRows, p.numColumns,
                p.numFolderRows, p.numFolderColumns, p.minAllAppsPredictionColumns,
                p.iconSize, p.iconTextSize, p.numHotSeatIcons, p.hotSeatIconSize,
                p.defaultLayoutId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public InvariantDeviceProfile(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        if (smallestSize == null || largestSize == null || realSize == null) {
            smallestSize = new Point();
            largestSize = new Point();
            realSize = new Point();
            display.getCurrentSizeRange(smallestSize, largestSize);
            display.getRealSize(realSize);
        }
        // This guarantees that width < height
        // 保证宽度小于高度
        minWidthDps = Utilities.dpiFromPx(Math.min(smallestSize.x, smallestSize.y), dm);
        minHeightDps = Utilities.dpiFromPx(Math.min(largestSize.x, largestSize.y), dm);
        // 最接近的配置
        ArrayList<InvariantDeviceProfile> closestProfileList =
                findClosestDeviceProfiles(minWidthDps, minHeightDps, getPredefinedDeviceProfiles());
        InvariantDeviceProfile closestProfile = closestProfileList.get(0);
        numRows = closestProfile.numRows;
        numColumns = closestProfile.numColumns;
        numHotSeatIcons = closestProfile.numHotSeatIcons;
        defaultLayoutId = closestProfile.defaultLayoutId;
        numFolderRows = closestProfile.numFolderRows;
        numFolderColumns = closestProfile.numFolderColumns;
        minAllAppsPredictionColumns = closestProfile.minAllAppsPredictionColumns;
        hotSeatAllAppsRank = numHotSeatIcons / 2;
        // --------------------------------------------------
        // TODO 抽空仔细看看
        InvariantDeviceProfile interpolatedDeviceProfileOut =
                invDistWeightedInterpolate(minWidthDps, minHeightDps, closestProfileList);
        iconSize = interpolatedDeviceProfileOut.iconSize;
        iconBitmapSize = Utilities.pxFromDp(iconSize, dm);
        iconTextSize = interpolatedDeviceProfileOut.iconTextSize;
        hotSeatIconSize = interpolatedDeviceProfileOut.hotSeatIconSize;
        //
        fillResIconDpi = getLauncherIconDensity(iconBitmapSize);
    }

    /**
     * 将配置根据接近程度进行排序
     *
     * @return 最相近的配置
     */
    private ArrayList<InvariantDeviceProfile> getPredefinedDeviceProfiles() {
        ArrayList<InvariantDeviceProfile> predefinedDeviceProfiles = new ArrayList<>();
        // width, height, #rows, #columns, #folder rows, #folder columns,
        // iconSize, iconTextSize, #hotseat, #hotseatIconSize, defaultLayoutId.
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Super Short Stubby",
                255, 300, 2, 3, 2, 3, 3, 48, 13, 3, 48, R.xml.default_workspace_4x4));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Shorter Stubby",
                255, 400, 3, 3, 3, 3, 3, 48, 13, 3, 48, R.xml.default_workspace_4x4));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Short Stubby",
                275, 420, 3, 4, 3, 4, 4, 48, 13, 5, 48, R.xml.default_workspace_4x4));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Stubby",
                255, 450, 3, 4, 3, 4, 4, 48, 13, 5, 48, R.xml.default_workspace_4x4));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Nexus S",
                296, 491.33f, 4, 4, 4, 4, 4, 48, 13, 5, 48, R.xml.default_workspace_4x4));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Nexus 4",
                335, 567, 4, 4, 4, 4, 4, DEFAULT_ICON_SIZE_DP, 13, 5, 56, R.xml.default_workspace_4x4));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Nexus 5",
                359, 567, 4, 4, 4, 4, 4, DEFAULT_ICON_SIZE_DP, 13, 5, 56, R.xml.default_workspace_4x4));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Large Phone",
                406, 694, 5, 5, 4, 4, 4, 64, 14.4f, 5, 56, R.xml.default_workspace_5x5));
        // The tablet profile is odd in that the landscape orientation
        // also includes the nav bar on the side
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Nexus 7",
                575, 904, 5, 6, 4, 5, 4, 72, 14.4f, 7, 60, R.xml.default_workspace_5x6));
        // Larger tablet profiles always have system bars on the top & bottom
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("Nexus 10",
                727, 1207, 5, 6, 4, 5, 4, 76, 14.4f, 7, 64, R.xml.default_workspace_5x6));
        predefinedDeviceProfiles.add(new InvariantDeviceProfile("20-inch Tablet",
                1527, 2527, 7, 7, 6, 6, 4, 100, 20, 7, 72, R.xml.default_workspace_4x4));

        return predefinedDeviceProfiles;
    }

    /**
     * 返回最接近的设备配置文件
     * 理解：手机尺寸为一个大的矩形(x0, y0), 配置中布局的宽高视为另一个矩形(x1, y1), 两个矩形放于同一个坐标原点
     * (x0, y0) 和 (x1, y1) 两点的距离即是距离
     * 对距离进行排序得到最短距离
     */
    ArrayList<InvariantDeviceProfile> findClosestDeviceProfiles(
            final float width, final float height, ArrayList<InvariantDeviceProfile> points) {
        // Sort the profiles by their closeness to the dimensions
        ArrayList<InvariantDeviceProfile> pointsByNearness = points;
        Collections.sort(pointsByNearness, new Comparator<InvariantDeviceProfile>() {
            @Override
            public int compare(InvariantDeviceProfile a, InvariantDeviceProfile b) {
                return (int) (dist(width, height, a.minWidthDps, a.minHeightDps)
                        - dist(width, height, b.minWidthDps, b.minHeightDps));
            }
        });
        return pointsByNearness;
    }


    /**
     * ？？？
     *
     * @param width
     * @param height
     * @param points
     * @return
     */
    InvariantDeviceProfile invDistWeightedInterpolate(float width, float height, ArrayList<InvariantDeviceProfile> points) {
        float weights = 0;
        InvariantDeviceProfile p = points.get(0);
        if (dist(width, height, p.minWidthDps, p.minHeightDps) == 0) {
            return p;
        }
        InvariantDeviceProfile out = new InvariantDeviceProfile();
        for (int i = 0; i < points.size() && i < KNEARESTNEIGHBOR; ++i) {
            p = new InvariantDeviceProfile(points.get(i));
            float w = weight(width, height, p.minWidthDps, p.minHeightDps, WEIGHT_POWER);
            weights += w;
            out.add(p.multiply(w));
        }
        return out.multiply(1.0f / weights);
    }

    /**
     * (x0, y0) 和 (x1, y1) 两点间的距离
     * 即一个长为 x1 - x0， 宽为 y1 - y0直角三角形的 斜边长
     *
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return 两点间的距离
     */
    float dist(float x0, float y0, float x1, float y1) {
        return (float) Math.hypot(x1 - x0, y1 - y0);
    }

    /**
     * 计算权重
     *
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param pow
     * @return
     */
    private float weight(float x0, float y0, float x1, float y1, float pow) {
        float d = dist(x0, y0, x1, y1);
        if (Float.compare(d, 0f) == 0) {
            // 表示重合, 权重无限大
            return Float.POSITIVE_INFINITY;
        }
        return (float) (WEIGHT_EFFICIENT / Math.pow(d, pow));
    }

    private void add(InvariantDeviceProfile p) {
        iconSize += p.iconSize;
        iconTextSize += p.iconTextSize;
        hotSeatIconSize += p.hotSeatIconSize;
    }

    private InvariantDeviceProfile multiply(float w) {
        iconSize *= w;
        iconTextSize *= w;
        hotSeatIconSize *= w;
        return this;
    }

    private int getLauncherIconDensity(int requiredSize) {
        // Densities typically defined by an app.
        int[] densityBuckets = new int[]{
                DisplayMetrics.DENSITY_LOW,
                DisplayMetrics.DENSITY_MEDIUM,
                DisplayMetrics.DENSITY_TV,
                DisplayMetrics.DENSITY_HIGH,
                DisplayMetrics.DENSITY_XHIGH,
                DisplayMetrics.DENSITY_XXHIGH,
                DisplayMetrics.DENSITY_XXXHIGH
        };

        int density = DisplayMetrics.DENSITY_XXXHIGH;
        for (int i = densityBuckets.length - 1; i >= 0; i--) {
            float expectedSize = ICON_SIZE_DEFINED_IN_APP_DP * densityBuckets[i]
                    / DisplayMetrics.DENSITY_DEFAULT;
            if (expectedSize >= requiredSize) {
                density = densityBuckets[i];
            }
        }

        return density;
    }
    // --------------------------------------------------

    public Point getSmallestSize() {
        return smallestSize;
    }

    public Point getLargestSize() {
        return largestSize;
    }

    public Point getRealSize() {
        return realSize;
    }
}