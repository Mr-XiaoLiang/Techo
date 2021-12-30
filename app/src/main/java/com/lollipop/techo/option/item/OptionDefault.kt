package com.lollipop.techo.option.item

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * @author lollipop
 * @date 2021/12/30 21:04
 */
enum class OptionDefault(
    /**
     * 操作图标
     */
    @DrawableRes
    val icon: Int,

    /**
     * 背景色
     */
    @ColorRes
    val backgroundColor: Int,

    /**
     * 操作名称
     */
    @StringRes
    val optionName: Int,
) {


    EDIT(
        0, 0, 0
    );

    companion object {
        private const val BASE_ID = 10000
    }

    fun new(): Option {
        return Option(icon, backgroundColor, optionName, BASE_ID + ordinal)
    }

}