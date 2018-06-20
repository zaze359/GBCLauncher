/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.zaze.launcher.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.zaze.launcher.LauncherAppState;
import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.data.entity.Favorites;
import com.zaze.launcher.data.entity.ShortcutInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {


    private static final Pattern sTrimPattern =
            Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private static final Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    public static final boolean ATLEAST_MARSHMALLOW =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    public static final boolean ATLEAST_LOLLIPOP_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static final boolean ATLEAST_LOLLIPOP =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static final boolean ATLEAST_KITKAT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    public static final boolean ATLEAST_JB_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean ATLEAST_JB_MR2 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;


    // --------------------------------------------------
    // --------------------------------------------------

    private static final Rect sOldBounds = new Rect();
    static int sColors[] = {0xffff0000, 0xff00ff00, 0xff0000ff};
    static int sColorIndex = 0;


    public static float dpiFromPx(int size, DisplayMetrics metrics) {
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }

    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }

    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, metrics));
    }

    // --------------------------------------------------
    // --------------------------------------------------

    public static boolean checkUserPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkAndRequestUserPermission(Activity activity, String permission, int requestCode) {
        boolean hasPermission = checkUserPermission(activity, permission);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
        return hasPermission;
    }

    public static void checkAndRequestUserPermission(Activity activity, String[] permissions, int requestCode) {
        Set<String> permissionSet = new HashSet<>();
        for (String permission : permissions) {
            if (!checkUserPermission(activity, permission)) {
                permissionSet.add(permission);
            }
        }
        ActivityCompat.requestPermissions(activity, permissionSet.toArray(new String[permissionSet.size()]), requestCode);
    }

    // --------------------------------------------------
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
                // Roll over to 1, not 0.
                if (newValue > 0x00FFFFFF) {
                    newValue = 1;
                }
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

    /**
     * 是否是从右到左布局
     *
     * @param res res
     * @return bool
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return ATLEAST_JB_MR1 && (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public static String findSystemApk(String action, PackageManager pm) {
        final Intent intent = new Intent(action);
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (info.activityInfo != null &&
                    (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return info.activityInfo.packageName;
            }
        }
        return null;
    }

    // --------------------------------------------------
    // --------------------------------------------------

    private static int getIconBitmapSize() {
        return LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize;
    }

    public static Bitmap createIconBitmap(Context context, Favorites favorites) {
        if (favorites.getIcon() != null) {
            byte[] data = favorites.getIcon().getBytes();
            try {
                return createIconBitmap(context, BitmapFactory.decodeByteArray(data, 0, data.length));
            } catch (Exception e) {
                return null;
            }
        }
        return null;

    }

    /**
     * Returns a bitmap which is of the appropriate size to be displayed as an icon
     */
    public static Bitmap createIconBitmap(Context context, Bitmap icon) {
        final int iconBitmapSize = getIconBitmapSize();
        if (iconBitmapSize == icon.getWidth() && iconBitmapSize == icon.getHeight()) {
            return icon;
        }
        return createIconBitmap(context, new BitmapDrawable(context.getResources(), icon));
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Context context, Drawable icon) {
        synchronized (sCanvas) {
            final int iconBitmapSize = getIconBitmapSize();

            int width = iconBitmapSize;
            int height = iconBitmapSize;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = iconBitmapSize;
            int textureHeight = iconBitmapSize;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth - width) / 2;
            final int top = (textureHeight - height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) {
                    sColorIndex = 0;
                }
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left + width, top + height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

    /**
     * Returns a bitmap suitable for the all apps view. If the package or the resource do not
     * exist, it returns null.
     */
    public static Bitmap createIconBitmap(Context context, String packageName, String resourceName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            Resources resources = packageManager.getResourcesForApplication(packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(resourceName, null, null);
                return createIconBitmap(context, ResourcesCompat.getDrawableForDensity(
                        resources, id, LauncherAppState.getInstance()
                                .getInvariantDeviceProfile().fillResIconDpi, null));
            }
        } catch (Exception e) {
            // Icon not found.
        }
        return null;
    }


    /**
     * 从收藏信息中 加载图标
     *
     * @param context      context
     * @param favorites    favorites
     * @param shortcutInfo shortcutInfo
     * @return
     */
    public static Bitmap loadIcon(Context context, Favorites favorites, ShortcutInfo shortcutInfo) {
        Bitmap icon = null;
        int iconType = favorites.getIconType();
        switch (iconType) {
            case LauncherSettings.ItemColumns.ICON_TYPE_RESOURCE:
                String packageName = favorites.getIconPackage();
                String resourceName = favorites.getIconResource();
                if (!TextUtils.isEmpty(packageName) || !TextUtils.isEmpty(resourceName)) {
                    shortcutInfo.iconResource = new Intent.ShortcutIconResource();
                    shortcutInfo.iconResource.packageName = packageName;
                    shortcutInfo.iconResource.resourceName = resourceName;
                    icon = Utilities.createIconBitmap(context, packageName, resourceName);
                }
                if (icon == null) {
                    // Failed to load from resource, try loading from DB.
                    icon = Utilities.createIconBitmap(context, favorites);
                }
                break;
            case LauncherSettings.ItemColumns.ICON_TYPE_BITMAP:
                icon = Utilities.createIconBitmap(context, favorites);
                shortcutInfo.customIcon = icon != null;
                break;
            default:
                break;
        }
        return icon;
    }

    // --------------------------------------------------
    // --------------------------------------------------

    /**
     * TODO ??? 这正则并不能去尾？
     * 去除字符串开始和结束位置的空格
     * Trims the string, removing all whitespace at the beginning and end of the string.
     * Non-breaking whitespaces are also removed.
     */
    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }
        // 只需从开始和结束处删除任何空格或java空格字符序列。
        Matcher m = sTrimPattern.matcher(s);
        return m.replaceAll("$1");
    }

}
