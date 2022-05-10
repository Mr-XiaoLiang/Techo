package com.lollipop.techo.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.util.onClick
import kotlin.math.abs

/**
 * @author lollipop
 * @date 2021/10/19 22:39
 * 圆形的展开动画管理组
 */
class CircleAnimationGroup<T: View> : Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    companion object {
        private const val MIN = 0F
        private const val MAX = 1F
        private const val CLOSED = 0.001F
        private const val OPENED = 0.999F
        private const val DURATION = 200L
        private const val LENGTH = MAX - MIN
    }

    private val inactiveViewList = ArrayList<T>()
    private val activeViewList = ArrayList<T>()
    private var centerView: View? = null
    private var listenerOption:ListenerOption? = null

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

    fun addPlanet(vararg views: T) {
        activeViewList.addAll(views)
    }

    fun addPlanet(vararg views: Pair<T, Boolean>) {
        views.forEach {
            if (it.second) {
                activeViewList.add(it.first)
            } else {
                inactiveViewList.add(it.first)
            }
        }
    }

    fun onPlanetClick(callback: (T) -> Unit) {
        activeViewList.forEach { planet ->
            (planet as View).onClick{
                callback(planet)
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

    fun bindListener(bind: ListenerOption.() -> Unit) {
        listenerOption = ListenerOption().apply(bind)
    }

    fun close() {
        listenerOption?.onHideCalled?.invoke()
        centerView?.post {
            listenerOption?.onHideStart?.invoke()
            valueAnimator.cancel()
            valueAnimator.duration = (abs(animationProgress - MIN) / LENGTH * DURATION).toLong()
            valueAnimator.setFloatValues(animationProgress, MIN)
            valueAnimator.start()
        }
    }

    fun open() {
        listenerOption?.onShowCalled?.invoke()
        centerView?.post {
            listenerOption?.onShowStart?.invoke()
            valueAnimator.cancel()
            valueAnimator.duration = (abs(animationProgress - MAX) / LENGTH * DURATION).toLong()
            valueAnimator.setFloatValues(animationProgress, MAX)
            valueAnimator.start()
        }
    }

    fun hide() {
        listenerOption?.onHideCalled?.invoke()
        listenerOption?.onHideStart?.invoke()
        valueAnimator.cancel()
        animationProgress = MIN
        update()
        visibleChange(false)
        listenerOption?.onHideEnd?.invoke()
    }

    fun show() {
        listenerOption?.onShowCalled?.invoke()
        listenerOption?.onShowStart?.invoke()
        valueAnimator.cancel()
        animationProgress = MAX
        update()
        visibleChange(true)
        listenerOption?.onShowEnd?.invoke()
    }

    fun destroy() {
        listenerOption = null
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
        listenerOption?.onProgressUpdate?.invoke(progress)
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
        if (animation == valueAnimator) {
            val closed = animationProgress <= CLOSED
            if (closed) {
                visibleChange(false)
                listenerOption?.onHideEnd?.invoke()
            } else {
                listenerOption?.onShowEnd?.invoke()
            }
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

    class ListenerOption {
        var onShowCalled: (() -> Unit) = {}
            private set
        var onShowStart: (() -> Unit) = {}
            private set
        var onShowEnd: (() -> Unit) = {}
            private set
        var onHideCalled: (() -> Unit) = {}
            private set
        var onHideStart: (() -> Unit) = {}
            private set
        var onHideEnd: (() -> Unit) = {}
            private set
        var onProgressUpdate: ((Float) -> Unit) = {}
            private set

        fun onShowCalled(callback: () -> Unit) {
            onShowCalled = callback
        }
        fun onShowStart(callback: () -> Unit) {
            onShowStart = callback
        }
        fun onShowEnd(callback: () -> Unit) {
            onShowEnd = callback
        }
        fun onHideCalled(callback: () -> Unit) {
            onHideCalled = callback
        }
        fun onHideStart(callback: () -> Unit) {
            onHideStart = callback
        }
        fun onHideEnd(callback: () -> Unit) {
            onHideEnd = callback
        }
        fun onProgressUpdate(callback: (Float) -> Unit) {
            onProgressUpdate = callback
        }
    }

}