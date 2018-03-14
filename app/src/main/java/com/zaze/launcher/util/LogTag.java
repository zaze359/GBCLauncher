package com.zaze.launcher.util;

import com.zaze.utils.log.ZTag;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2017-12-17 - 22:46
 */
public interface LogTag extends ZTag {
    String TAG_LIFECYCLE = "Lifecycle" + TAG_BASE;
    String TAG_LOADER = "Loader" + TAG_BASE;
    String TAG_CELL_LAYOUT = "CellLayout" + TAG_BASE;
    String TAG_SHORTCUT_CONTAINER = "ShortcutContainer" + TAG_BASE;
    String TAG_DOC = "Doc" + TAG_BASE;
}
