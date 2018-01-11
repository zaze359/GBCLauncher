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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 一些不变的配置
 */
public class InvariantDeviceProfile {
    private static float DEFAULT_ICON_SIZE_DP = 60;
    private static final float ICON_SIZE_DEFINED_IN_APP_DP = 48;
    // --------------------------------------------------

    // Profile-defining invariant properties
    String name;
    float minWidthDps;
    float minHeightDps;
    /**
     * Number of icons per row and column in the workspace.
     */
    public int numRows;
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
    int defaultLayoutId;

    int iconBitmapSize;
    int fillResIconDpi;

    public int hotSeatAllAppsRank;


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

    public InvariantDeviceProfile() {
        ArrayList<InvariantDeviceProfile> closestProfiles =
                findClosestDeviceProfiles(minWidthDps, minHeightDps, getPredefinedDeviceProfiles());
        InvariantDeviceProfile closestProfile = closestProfiles.get(0);
        numRows = closestProfile.numRows;
        numColumns = closestProfile.numColumns;
        numHotSeatIcons = closestProfile.numHotSeatIcons;
        defaultLayoutId = closestProfile.defaultLayoutId;
        numFolderRows = closestProfile.numFolderRows;
        numFolderColumns = closestProfile.numFolderColumns;
        minAllAppsPredictionColumns = closestProfile.minAllAppsPredictionColumns;
        hotSeatAllAppsRank = numHotSeatIcons / 2;
    }

    ArrayList<InvariantDeviceProfile> getPredefinedDeviceProfiles() {
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
     * (x0, y0) 和 (x1, y1) 两点的长度
     * 即一个长为 x1 - x0， 宽为 y1 - y0直角三角形的 斜边长
     *
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     */
    float dist(float x0, float y0, float x1, float y1) {
        return (float) Math.hypot(x1 - x0, y1 - y0);
    }
}