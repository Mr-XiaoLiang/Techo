package com.lollipop.techo.option.item

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lollipop.techo.R

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
        R.drawable.ic_baseline_edit_24,
        R.color.optionEdit,
        R.string.optionEdit
    ),
    STYLE(
        R.drawable.ic_baseline_color_lens_24,
        R.color.optionStyle,
        R.string.optionStyle
    ),
    IMAGE(
        R.drawable.ic_outline_insert_photo_24,
        R.color.optionImage,
        R.string.optionImage
    ),
    DELETE(
        R.drawable.ic_baseline_delete_24,
        R.color.optionDelete,
        R.string.optionDelete
    ),
    TOPPING(
        R.drawable.ic_topping,
        R.color.optionTopping,
        R.string.optionTopping
    ),
    TOPPING_NOT(
        R.drawable.ic_topping_not,
        R.color.optionToppingNot,
        R.string.optionToppingNot
    ),
    LIKE(
        R.drawable.ic_like,
        R.color.optionLike,
        R.string.optionLike
    ),
    UNLIKE(
        R.drawable.ic_unlike,
        R.color.optionUnlike,
        R.string.optionUnlike
    ),
    ;

    companion object {
        private const val BASE_ID = 10000

        fun findByOptionId(id: Int): OptionDefault? {
            val index = id - BASE_ID
            val values = values()
            if (index in values.indices) {
                return values[index]
            }
            return null
        }
    }

    fun new(): Option {
        return Option(icon, backgroundColor, optionName, BASE_ID + ordinal)
    }

}