package com.lollipop.techo.util

import android.view.View

/**
 * @author lollipop
 * @date 2021/10/19 22:39
 * 圆形的展开动画管理组
 */
class CircleAnimationGroup {

    private val planetViewList = ArrayList<View>()
    private var centerView: View? = null

    fun addPlanet(vararg views: View) {
        planetViewList.addAll(views)
    }

    fun removePlanet(vararg views: View) {
        planetViewList.removeAll(views)
    }

    fun close() {
        // TODO 动画的关闭
    }

    fun open() {
        // TODO 动画的展开
    }

    fun hide() {
        // TODO 静态的隐藏
    }

    fun show() {
        // TODO 静态的显示
    }

    fun destroy() {
        // TODO 停止动画

        planetViewList.clear()
        centerView = null
    }

}