package com.zaze.launcher.data.entity;

import android.content.ComponentName;
import android.content.Intent;

import com.zaze.launcher.LauncherSettings;
import com.zaze.launcher.view.IconCache;

/**
 * Description :
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
     */
    public static final int FLAG_AUTOINTALL_ICON = 2; //0B10;

    /**
     * The icon is being installed. If {@link FLAG_RESTORED_ICON} or {@link FLAG_AUTOINTALL_ICON}
     * is set, then the icon is either being installed or is in a broken state.
     */
    public static final int FLAG_INSTALL_SESSION_ACTIVE = 4; // 0B100;

    /**
     * Indicates that the widget restore has started.
     */
    public static final int FLAG_RESTORE_STARTED = 8; //0B1000;

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
     * Indicates whether we're using a low res icon
     */
    public boolean usingLowResIcon;


    // --------------------------------------------------

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


    public void updateIcon(IconCache iconCache, boolean useLowRes) {
        if (itemType == LauncherSettings.ItemColumns.ITEM_TYPE_APPLICATION) {
            // TODO: 2018/3/2
//            iconCache.getTitleAndIcon(this, promisedIntent != null ? promisedIntent : intent, user, useLowRes);
        }
    }
}
