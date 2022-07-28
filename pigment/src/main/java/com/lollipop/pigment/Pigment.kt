package com.lollipop.pigment

/**
 * 颜料
 * 他表示装饰主题色变化的内容
 */
class Pigment(
    /**
     * 主题色
     */
    val primary: Int,

    /**
     * 主题色变体
     * 一般表示比主题色更深或者更浅的颜色，
     * 用于表达主题色，但是和主题色区分
     */
    val primaryVariant: Int,

    /**
     * 在主题色之上的内容的颜色
     */
    val onPrimaryTitle: Int,

    /**
     * 在主题色之上的内容的颜色
     */
    val onPrimaryBody: Int,

    /**
     * 次要颜色
     * 它是一种撞色
     * 用于装饰主题色空间内容醒目元素
     */
    val secondary: Int,

    /**
     * 次要颜色的变体
     * 一般是次要颜色的加深或是变浅
     * 用于表达次要颜色的同时和次要颜色区分
     */
    val secondaryVariant: Int,

    /**
     * 在次要颜色之上的内容的颜色
     */
    val onSecondaryTitle: Int,

    /**
     * 在次要颜色之上的内容的颜色
     */
    val onSecondaryBody: Int,
)