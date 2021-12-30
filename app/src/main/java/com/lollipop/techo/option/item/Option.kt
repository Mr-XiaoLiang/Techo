package com.lollipop.techo.option.item

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * @author lollipop
 * @date 2021/12/30 21:03
 */
class Option(
    /**
     * 操作图标
     */
    @DrawableRes
    val icon: Int,

    /**
     * 背景色
     */
    @DrawableRes
    val background: Int,

    /**
     * 操作名称
     */
    @StringRes
    val name: Int,

    /**
     * 操作ID
     */
    val id: Int
)