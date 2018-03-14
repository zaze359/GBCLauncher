package com.zaze.launcher;

import com.zaze.launcher.data.entity.ItemInfo;

import java.util.List;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2018-03-10 - 16:09
 */
public interface LauncherContract {
    interface View {
        /**
         * 返回当前工作区位置
         *
         * @return 当前工作区位置
         */
        int getCurrentWorkspaceScreen();

        /**
         * 告诉工作区 , 我们准备开始绑定数据到视图
         */
        void startBinding();

        /**
         * 绑定Screens
         *
         * @param orderedScreens orderedScreens
         */
        void bindScreens(List<Long> orderedScreens);

        /**
         * 同步绑定页面时
         *
         * @param currentScreen currentScreen
         */
        void onPageBoundSynchronously(int currentScreen);

        /**
         * 绑定items
         *
         * @param items
         * @param start             开始位置
         * @param end               结束位置
         * @param forceAnimateIcons
         */
        void bindItems(List<ItemInfo> items, int start, int end, boolean forceAnimateIcons);
    }

    interface Presenter {

    }
}
