package com.zaze.launcher.data.entity;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;

import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.view.FolderIcon;
import com.zaze.launcher.view.IconCache;
import com.zaze.utils.log.ZLog;
import com.zaze.utils.log.ZTag;

/**
 * Description : 工作区或者文件夹中可操作的图标
 *
 * @author : ZAZE
 * @version : 2018-01-16 - 14:09
 */
public class ShortcutInfo extends ItemInfo {

    public static final int DEFAULT = 0;

    /**
     * The shortcut was restored from a backup and it not ready to be used. This is automatically
     * set during backup/restore
     */
    public static final int FLAG_RESTORED_ICON = 1;

    /**
     * The icon was added as an auto-install app, and is not ready to be used. This flag can't
     * be present along with {@link #FLAG_RESTORED_ICON}, and is set during default layout
     * parsing.
     * 添加为自动安装应用程序
     * 0B10
     */
    public static final int FLAG_AUTOINTALL_ICON = 2;

    /**
     * The icon is being installed. If {@link #FLAG_RESTORED_ICON} or {@link #FLAG_AUTOINTALL_ICON}
     * is set, then the icon is either being installed or is in a broken state.
     * <p>
     * 0B100
     */
    public static final int FLAG_INSTALL_SESSION_ACTIVE = 4;

    /**
     * Indicates that the widget restore has started.
     * <p>
     * 0B1000
     */
    public static final int FLAG_RESTORE_STARTED = 8;

    /**
     * Indicates if it represents a common type mentioned in {@link CommonAppTypeParser}.
     * Upto 15 different types supported.
     */
    public static final int FLAG_RESTORED_APP_TYPE = 0B0011110000;


    // --------------------------------------------------
    // --------------------------------------------------

    /**
     * The intent used to start the application.
     */
    public Intent intent;

    /**
     * If this shortcut is a placeholder, then intent will be a market intent for the package, and
     * this will hold the original intent from the database.  Otherwise, null.
     * Refer {@link #FLAG_RESTORE_PENDING}, {@link #FLAG_INSTALL_PENDING}
     */
    public Intent promisedIntent;

    /**
     * Could be disabled, if the the app is installed but unavailable (eg. in safe mode or when
     * sd-card is not available).
     */
    public int isDisabled = DEFAULT;


    public int status;

    /**
     * The installation progress [0-100] of the package that this shortcut represents.
     */
    private int mInstallProgress;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * 作为应用程序资源的快捷图标
     */
    public Intent.ShortcutIconResource iconResource;

    /**
     * The application icon.
     */
    private Bitmap mIcon;

    /**
     * Indicates that the icon is disabled due to safe mode restrictions.
     */
    public static final int FLAG_DISABLED_SAFEMODE = 1;

    /**
     * Indicates that the icon is disabled as the app is not available.
     */
    public static final int FLAG_DISABLED_NOT_AVAILABLE = 2;

    /**
     * 表明图标是来自应用程序资源(if false)还是用户自定义Bitmap(if true.)
     */
    public boolean customIcon;

    /**
     * 指定我们是否使用默认的备用icon来替代应用icon
     */
    public boolean usingFallbackIcon;

    /**
     * 指定我们是否使用低分辨率的icon
     */
    public boolean usingLowResIcon;

    /**
     * TODO move this to {@link status}
     */
    public int flags = 0;

    // --------------------------------------------------

    public ShortcutInfo() {
        super();
        itemType = LauncherSettings.ItemColumns.ITEM_TYPE_SHORTCUT;
    }

    @Override
    public ItemInfo setValues(Favorites favorites) {
        this.rank = favorites.getRank();
        return super.setValues(favorites);
    }

    public ComponentName getTargetComponent() {
        return promisedIntent != null ? promisedIntent.getComponent() : intent.getComponent();
    }

    public int getInstallProgress() {
        return mInstallProgress;
    }

    public void setInstallProgress(int progress) {
        mInstallProgress = progress;
        status |= FLAG_INSTALL_SESSION_ACTIVE;
    }

    // --------------------------------------------------
    public void setIcon(Bitmap mIcon) {
        this.mIcon = mIcon;
    }

    public Bitmap getIcon(IconCache iconCache) {
        if (mIcon == null) {
            updateIcon(iconCache);
        }
        return mIcon;
    }

    public void updateIcon(IconCache iconCache) {
        updateIcon(iconCache, shouldUseLowResIcon());
    }

    public void updateIcon(IconCache iconCache, boolean useLowRes) {
        if (itemType == LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION) {
            // TODO: 2018/3/2
            ZLog.e(ZTag.TAG_DEBUG, "TODO : updateIcon()");
//            iconCache.getTitleAndIcon(this, promisedIntent != null ? promisedIntent : intent, user, useLowRes);
        }
    }

    public boolean shouldUseLowResIcon() {
        return usingLowResIcon && container >= 0 && rank >= FolderIcon.NUM_ITEMS_IN_PREVIEW;
    }

    public final boolean isPromise() {
        return hasStatusFlag(FLAG_RESTORED_ICON | FLAG_AUTOINTALL_ICON);
    }

    public boolean hasStatusFlag(int flag) {
        return (status & flag) != 0;
    }
}
