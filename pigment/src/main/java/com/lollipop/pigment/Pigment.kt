package com.lollipop.pigment

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
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
    val blendMode: BlendMode
) {

    companion object {

        fun valueOf(primary: Int, secondary: Int, blendMode: BlendMode): Pigment {
            return Pigment(Color.valueOf(primary), Color.valueOf(secondary), blendMode)
        }

        fun getBlendByNightMode(context: Context): BlendMode {
            return if (isNightMode(context)) {
                BlendMode.Dark
            } else {
                BlendMode.Light
            }
        }

        fun isNightMode(context: Context): Boolean {
            return isNightMode(context.resources)
        }

        fun isNightMode(resources: Resources): Boolean {
            return isNightMode(resources.configuration)
        }

        fun isNightMode(config: Configuration): Boolean {
            return isNightMode(config.uiMode)
        }

        fun isNightMode(uiMode: Int): Boolean {
            return uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }

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
    val backgroundColor: Int by lazy {
        blendMode.background(primaryColor)
    }

    /**
     * 背景色之上的标题
     */
    val onBackgroundTitle: Int by lazy {
        blendMode.title(backgroundColor)
    }

    /**
     * 背景色之上的内容体
     */
    val onBackgroundBody: Int by lazy {
        blendMode.title(backgroundColor)
    }

    /**
     * 模式所对应的极致的颜色
     * 比如，在深色模式下，它会是黑色，浅色模式下，它会是白色
     */
    val extreme: Int by lazy {
        blendMode.extreme
    }

    /**
     * 极致色彩的反转色
     */
    val extremeReversal: Int by lazy {
        blendMode.extremeReversal
    }

    /**
     * 极致色彩为背景时的标题颜色
     */
    val onExtremeTitle: Int by lazy {
        blendMode.title(extreme)
    }

    /**
     * 极致色彩为背景时的内容颜色
     */
    val onExtremeBody: Int by lazy {
        blendMode.body(extreme)
    }
}