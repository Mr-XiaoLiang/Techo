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
     * 混合模式
     */
    private val blendMode: BlendMode
) {

    val primaryColor: Int by lazy {
        blendMode.original(primary.toArgb())
    }

    val secondaryColor: Int by lazy {
        blendMode.original(secondary?.toArgb() ?: primaryColor)
    }

    /**
     * 主题色变体
     * 一般表示比主题色更深或者更浅的颜色，
     * 用于表达主题色，但是和主题色区分
     */
    val primaryVariant: Int by lazy {
        blendMode.variant(primaryColor)
    }

    /**
     * 在主题色之上的内容的颜色
     */
    val onPrimaryTitle: Int by lazy {
        blendMode.title(primaryColor)
    }

    /**
     * 在主题色之上的内容的颜色
     */
    val onPrimaryBody: Int by lazy {
        blendMode.body(primaryColor)
    }

    /**
     * 次要颜色的变体
     * 一般是次要颜色的加深或是变浅
     * 用于表达次要颜色的同时和次要颜色区分
     */
    val secondaryVariant: Int by lazy {
        blendMode.variant(secondaryColor)
    }

    /**
     * 在次要颜色之上的内容的颜色
     */
    val onSecondaryTitle: Int by lazy {
        blendMode.title(secondaryColor)
    }

    /**
     * 在次要颜色之上的内容的颜色
     */
    val onSecondaryBody: Int by lazy {
        blendMode.body(secondaryColor)
    }

    /**
     * 背景色
     */
    val background: Int by lazy {
        blendMode.background(primaryColor)
    }

    /**
     * 背景色之上的标题
     */
    val onBackgroundTitle: Int by lazy {
        blendMode.title(background)
    }

    /**
     * 背景色之上的内容体
     */
    val onBackgroundBody: Int by lazy {
        blendMode.title(background)
    }
}