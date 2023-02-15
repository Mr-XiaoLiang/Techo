package com.lollipop.browser.main.launcher

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lollipop.browser.R

object LauncherManager {

    fun load(callback: (List<LauncherInfo>) -> Unit) {
        TODO()
    }

    fun add(info: LauncherInfo) {
        TODO()
    }

    fun move(info: LauncherInfo, index: Int) {
        TODO()
    }

// 谷歌徽标 icon by Icons8

    private enum class DefaultLauncher(
        val id: Int,
        @StringRes
        val label: Int,
        @DrawableRes
        val icon: Int,
        @ColorRes
        val iconTint: Int,
        @DrawableRes
        val background: Int,
        val url: String
    ) {

        Baidu(
            id = -1,
            label = R.string.label_baidu,
            icon = R.drawable.ic_baidu,
            iconTint = R.color.gray_0,
            background = 0,
            url = ""
        ),
        Bing(
            id = -2,
            label = R.string.label_bing,
            icon = R.drawable.ic_bing,
            iconTint = R.color.gray_0,
            background = 0,
            url = ""
        ),
        Google(
            id = -3,
            label = R.string.label_google,
            icon = 0,
            iconTint = R.color.gray_0,
            background = 0,
            url = ""
        ),

    }

}