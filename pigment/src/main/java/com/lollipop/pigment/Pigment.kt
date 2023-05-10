package com.lollipop.pigment

import android.graphics.Color

/**
 * 颜料
 * 他表示装饰主题色变化的内容
 */
class Pigment(
    /**
     * 主题色
     */
    val primary: Color,

    /**
     * 次要颜色
     * 它是一种撞色
     * 用于装饰主题色空间内容醒目元素
     */
    val secondary: Color?,

    /**
     * 次要颜色
     * 它是一种撞色
     * 用于装饰主题色空间内容醒目元素
     */
    val tertiary: Color?

) {

    val primaryColor: Int by lazy {
        primary.toArgb()
    }

    val secondaryColor: Int by lazy {
        secondary?.toArgb() ?: primaryColor
    }

    val tertiaryColor: Int by lazy {
        tertiary?.toArgb() ?: secondaryColor
    }

    /**
     * 主题色变体
     * 一般表示比主题色更深或者更浅的颜色，
     * 用于表达主题色，但是和主题色区分
     */
//    val primaryVariant: Int,

    /**
     * 在主题色之上的内容的颜色
     */
//    val onPrimaryTitle: Int,

    /**
     * 在主题色之上的内容的颜色
     */
//    val onPrimaryBody: Int,
    /**
     * 次要颜色的变体
     * 一般是次要颜色的加深或是变浅
     * 用于表达次要颜色的同时和次要颜色区分
     */
//    val secondaryVariant: Int,

    /**
     * 在次要颜色之上的内容的颜色
     */
//    val onSecondaryTitle: Int,

    /**
     * 在次要颜色之上的内容的颜色
     */
//    val onSecondaryBody: Int,
}