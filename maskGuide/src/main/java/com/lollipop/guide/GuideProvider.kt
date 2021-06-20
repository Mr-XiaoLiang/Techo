package com.lollipop.guide

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

/**
 * @author lollipop
 * @date 2021/5/22 19:10
 * 蒙层呈现器
 */
abstract class GuideProvider {

    protected val guideBounds = Rect()

    protected val guideInsets = Rect()

    protected val targetBounds = Rect()

    protected var targetStep: GuideStep? = null

    open val supportAnimation = false

    private var guideManager: GuideManager? = null

    abstract fun support(step: GuideStep): Boolean

    var view: View? = null
        private set

    fun getView(group: ViewGroup): View {
        val providerView = view
        if (providerView == null) {
            val newView = onCreateView(group)
            view = newView
            onViewCreated(newView)
            return newView
        }
        return providerView
    }

    protected open fun onCreateView(group: ViewGroup): View {
        throw RuntimeException("no view")
    }

    protected open fun onViewCreated(view: View) {

    }

    fun destroy() {
        onDestroy()
    }

    protected open fun onDestroy() {

    }

    internal fun bindGuideManager(guideManager: GuideManager?) {
        this.guideManager = guideManager
    }

    fun setGuideBounds(width: Int, height: Int, left: Int, top: Int, right: Int, bottom: Int) {
        guideInsets.set(left, top, right, bottom)
        guideBounds.set(left, top, left + width, top + height)
        onGuideBoundsChanged(width, height, left, top, right, bottom)
    }

    abstract fun onGuideBoundsChanged(
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    )

    fun setTargetBounds(left: Int, top: Int, right: Int, bottom: Int) {
        targetBounds.set(left, top, right, bottom)
        onTargetBoundsChange(left, top, right, bottom)
    }

    abstract fun onTargetBoundsChange(left: Int, top: Int, right: Int, bottom: Int)

    fun updateGuideStep(step: GuideStep) {
        targetStep = step
        onTargetChange(step)
    }

    abstract fun onTargetChange(step: GuideStep)

    open fun onAnimation(isPositive: Boolean, progress: Float) {}

    open fun onAnimationStart(isPositive: Boolean) {}

    open fun onAnimationEnd(isPositive: Boolean) {}

    protected fun callNextStep() {
        guideManager?.nextStep()
    }

}