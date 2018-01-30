package com.zaze.launcher.util;

import com.zaze.utils.log.ZTag;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2017-12-17 - 22:46
 */
public interface LogTag extends ZTag {
    String TAG_LIFECYCLE = "Lifecycle[生命周期]" + TAG_BASE;
    String TAG_CELL_LAYOUT = "CellLayout[]" + TAG_BASE;
    String TAG_SHORTCUT_CONTAINER = "ShortcutAndWidgetContainer[]" + TAG_BASE;
    String TAG_DOC = "XML[解析xml]" + TAG_BASE;
    String TAG_LAYOUT = "Layout[加载布局]" + TAG_BASE;
}
