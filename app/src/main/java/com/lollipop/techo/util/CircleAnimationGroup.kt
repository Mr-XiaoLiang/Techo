package com.lollipop.techo.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.view.isInvisible
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

    private val planetViewList = ArrayList<View>()
    private var centerView: View? = null

    private var animationProgress = 0F

    private val valueAnimator by lazy {
        ValueAnimator().apply {
            addListener(this@CircleAnimationGroup)
            addUpdateListener(this@CircleAnimationGroup)
        }
    }

    fun addPlanet(vararg views: View) {
        planetViewList.addAll(views)
    }

    fun removePlanet(vararg views: View) {
        planetViewList.removeAll(views)
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
        visibleChange(true)
    }

    fun show() {
        valueAnimator.cancel()
        animationProgress = MAX
        update()
        visibleChange(false)
    }

    fun destroy() {
        valueAnimator.cancel()
        planetViewList.clear()
        centerView = null
    }

    private fun visibleChange(invisible: Boolean) {
        planetViewList.forEach {
            it.isInvisible = invisible
        }
    }

    private fun update() {
        val progress = animationProgress
        val center = centerView ?: return
        val centerX = half(center.left, center.right)
        val centerY = half(center.top, center.bottom)
        planetViewList.forEach {
            updateView(it, centerX, centerY, progress)
        }
    }

    private fun updateView(planet: View, centerX: Int, centerY: Int, progress: Float) {
        val planetX = half(planet.left, planet.right)
        val planetY = half(planet.top, planet.bottom)
        planet.translationX = (centerX - planetX) * progress
        planet.translationY = (centerY - planetY) * progress
    }

    private fun half(value1: Int, value2: Int): Int {
        return (value1 + value2) / 2
    }

    override fun onAnimationStart(animation: Animator?) {
        if (animation == valueAnimator) {
            visibleChange(false)
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        if (animation == valueAnimator && animationProgress >= CLOSED) {
            visibleChange(true)
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

}