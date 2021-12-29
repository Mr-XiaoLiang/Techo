package com.lollipop.techo.util

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import kotlin.math.abs

/**
 * @author lollipop
 * @date 2021/12/21 21:27
 */
class AnimationHelper(
    var duration: Long = 300L,
    private val onUpdate: (Float) -> Unit
) : ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    companion object {
        const val PROGRESS_MAX = 1F
        const val PROGRESS_MIN = 0F

        const val THRESHOLD = 0.001F
    }

    private val animator: ValueAnimator by lazy {
        ValueAnimator().apply {
            addUpdateListener(this@AnimationHelper)
            addListener(this@AnimationHelper)
            repeatCount
        }
    }

    private var progress: Float = PROGRESS_MIN

    private var onStartCallback: ((Float) -> Unit)? = null

    private var onEndCallback: ((Float) -> Unit)? = null

    private var startProgress = PROGRESS_MIN

    private var endProgress = PROGRESS_MAX

    fun setInterpolator(run: TimeInterpolator) {
        animator.interpolator = run
    }

    fun reset(progress: Float) {
        this.progress = progress
    }

    fun repeatCount(count: Int) {
        this.animator.repeatCount = count
    }

    fun repeatInfinite(isInfinite: Boolean) {
        repeatCount(if (isInfinite) {ValueAnimator.INFINITE} else { 0 })
    }

    fun progressIs(float: Float): Boolean {
        return abs(float - progress) <= THRESHOLD
    }

    fun preload() {
        close(false)
    }

    fun open(isAnimation: Boolean = true) {
        run(isAnimation, PROGRESS_MIN, PROGRESS_MAX)
    }

    fun close(isAnimation: Boolean = true) {
        run(isAnimation, PROGRESS_MAX, PROGRESS_MIN)
    }

    private fun run(
        isAnimation: Boolean = true,
        startProgress: Float = PROGRESS_MAX,
        endProgress: Float = PROGRESS_MIN,
        delay: Long = 0
    ) {
        this.startProgress = startProgress
        this.endProgress = endProgress
        if (isAnimation) {
            doAnimation(delay)
        } else {
            onProgressChange(endProgress)
            onAnimationEnd(animator)
        }
    }

    fun onStart(callback: (Float) -> Unit) {
        this.onStartCallback = callback
    }

    fun onEnd(callback: (Float) -> Unit) {
        this.onEndCallback = callback
    }

    fun destroy() {
        animator.cancel()
    }

    private fun doAnimation(delay: Long) {
        animator.cancel()
        animator.startDelay = delay
        val start = startProgress
        val end = endProgress
        val d = (abs(progress - end) / abs(start - end) * duration).toLong()
        animator.setFloatValues(progress, end)
        animator.duration = d
        animator.start()
    }

    private fun onProgressChange(progress: Float) {
        this.progress = progress
        onUpdate.invoke(progress)
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == animator) {
            onProgressChange(animation.animatedValue as Float)
        }
    }

    override fun onAnimationStart(animation: Animator?) {
        if (animation == animator) {
            onStartCallback?.invoke(progress)
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        if (animation == animator) {
            onEndCallback?.invoke(progress)
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }


}