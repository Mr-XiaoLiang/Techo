package com.lollipop.techo.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import kotlin.math.abs

/**
 * @author lollipop
 * @date 2021/10/19 22:39
 * 圆形的展开动画管理组
 */
class CircleAnimationGroup : Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    companion object {
        private const val MIN = 0F
        private const val MAX = 1F
        private const val CLOSED = 0.001F
        private const val OPENED = 0.999F
        private const val DURATION = 300L
        private const val LENGTH = MAX - MIN
    }

    private val inactiveViewList = ArrayList<View>()
    private val activeViewList = ArrayList<View>()
    private var centerView: View? = null
    private var onProgressUpdateListener: OnProgressUpdateListener? = null

    private var animationProgress = 0F

    private val valueAnimator by lazy {
        ValueAnimator().apply {
            addListener(this@CircleAnimationGroup)
            addUpdateListener(this@CircleAnimationGroup)
        }
    }

    val isOpened: Boolean
        get() {
            return animationProgress >= OPENED
        }

    fun addPlanet(vararg views: View) {
        activeViewList.addAll(views)
    }

    fun addPlanet(vararg views: Pair<View, Boolean>) {
        views.forEach {
            if (it.second) {
                activeViewList.add(it.first)
            } else {
                inactiveViewList.add(it.first)
            }
        }
    }

    fun removePlanet(vararg views: View) {
        val elements = views.toSet()
        activeViewList.removeAll(elements)
        inactiveViewList.removeAll(elements)
    }

    fun setCenterView(view: View) {
        centerView = view
    }

    fun onProgressUpdate(listener: OnProgressUpdateListener) {
        onProgressUpdateListener = listener
    }

    fun close() {
        centerView?.post {
            valueAnimator.cancel()
            valueAnimator.duration = (abs(animationProgress - MIN) / LENGTH * DURATION).toLong()
            valueAnimator.setFloatValues(animationProgress, MIN)
            valueAnimator.start()
        }
    }

    fun open() {
        centerView?.post {
            valueAnimator.cancel()
            valueAnimator.duration = (abs(animationProgress - MAX) / LENGTH * DURATION).toLong()
            valueAnimator.setFloatValues(animationProgress, MAX)
            valueAnimator.start()
        }
    }

    fun hide() {
        valueAnimator.cancel()
        animationProgress = MIN
        update()
        visibleChange(false)
    }

    fun show() {
        valueAnimator.cancel()
        animationProgress = MAX
        update()
        visibleChange(true)
    }

    fun destroy() {
        onProgressUpdateListener = null
        inactiveViewList.clear()
        activeViewList.clear()
        centerView = null
        valueAnimator.cancel()
    }

    private fun visibleChange(visible: Boolean) {
        activeViewList.forEach {
            it.isInvisible = !visible
        }
        if (!visible) {
            inactiveViewList.forEach {
                it.isVisible = false
            }
        }
    }

    private fun update() {
        val progress = animationProgress
        val center = centerView ?: return
        val centerX = half(center.left, center.right)
        val centerY = half(center.top, center.bottom)
        activeViewList.forEach {
            updateView(it, centerX, centerY, progress)
        }
        onProgressUpdateListener?.onProgressUpdate(progress)
    }

    private fun updateView(planet: View, centerX: Int, centerY: Int, progress: Float) {
        val planetX = half(planet.left, planet.right)
        val planetY = half(planet.top, planet.bottom)
        planet.translationX = (centerX - planetX) * (1 - progress)
        planet.translationY = (centerY - planetY) * (1 - progress)
    }

    private fun half(value1: Int, value2: Int): Int {
        return (value1 + value2) / 2
    }

    override fun onAnimationStart(animation: Animator?) {
        if (animation == valueAnimator) {
            visibleChange(true)
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        if (animation == valueAnimator && animationProgress <= CLOSED) {
            visibleChange(false)
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == valueAnimator) {
            animationProgress = animation.animatedValue as Float
            update()
        }
    }

    fun interface OnProgressUpdateListener {
        fun onProgressUpdate(progress: Float)
    }

}