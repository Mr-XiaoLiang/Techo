package com.lollipop.guide

import android.graphics.Rect

/**
 * @author lollipop
 * @date 2021/5/22 19:10
 * 蒙层呈现器
 */
abstract class GuideProvider {

    protected val guideBounds = Rect()

    protected val targetBounds = Rect()

    protected var targetStep: GuideStep? = null

    abstract fun support(step: GuideStep): Boolean

    fun setGuideBounds(left: Int, top: Int, right: Int, bottom: Int) {
        guideBounds.set(left, top, right, bottom)
        onGuideBoundsChanged(left, top, right, bottom)
    }

    abstract fun onGuideBoundsChanged(left: Int, top: Int, right: Int, bottom: Int)

    fun setTargetBounds(left: Int, top: Int, right: Int, bottom: Int) {
        targetBounds.set(left, top, right, bottom)
    }

    fun updateGuideStep(step: GuideStep) {
        targetStep = step
        onTargetChange(step)
    }

    abstract fun onTargetChange(step: GuideStep)

    open fun onAnimation(progress: Float) {}

}