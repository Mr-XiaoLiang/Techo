package com.lollipop.qr.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.qr.reader.OnCameraFocusChangedListener

class FocusAnimationHelper(
    val view: () -> View?
) : OnCameraFocusChangedListener, ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener {

    companion object {
        const val ANIMATION_DURATION = 300L
    }

    private val valueAnimator = ValueAnimator().apply {
        addUpdateListener(this@FocusAnimationHelper)
        addListener(this@FocusAnimationHelper)
    }

    override fun onCameraFocusChanged(isSuccessful: Boolean, x: Float, y: Float) {
        val v = view()
        if (v == null) {
            valueAnimator.cancel()
            return
        }
        val hLength = v.width / 2
        val vLength = v.height / 2
        val offsetX = x - (v.left + hLength)
        val offsetY = y - (v.top + vLength)
        v.offsetLeftAndRight(offsetX.toInt())
        v.offsetTopAndBottom(offsetY.toInt())
        restart(isSuccessful)
    }

    private fun restart(isSuccessful: Boolean) {
        valueAnimator.cancel()
        val v = view() ?: return
        v.isInvisible = true
        if (isSuccessful) {
            valueAnimator.setFloatValues(0F, 1F, 2F)
            valueAnimator.duration = ANIMATION_DURATION * 2
        } else {
            valueAnimator.setFloatValues(0F, 0.5F, 0F)
            valueAnimator.duration = ANIMATION_DURATION
        }
        valueAnimator.start()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        if (animation === valueAnimator) {
            val v = view() ?: return
            val progress = animation.animatedValue as? Float ?: return
            val scale = (2F - progress).coerceAtMost(2F).coerceAtLeast(1F)
            val alpha = progress.coerceAtMost(1F).coerceAtLeast(0F)
            v.scaleX = scale
            v.scaleY = scale
            v.alpha = alpha
        }
    }

    override fun onAnimationStart(animation: Animator) {
        if (animation === valueAnimator) {
            view()?.isVisible = true
        }
    }

    override fun onAnimationEnd(animation: Animator) {
        if (animation === valueAnimator) {
            view()?.isInvisible = true
        }
    }

    override fun onAnimationCancel(animation: Animator) {
        if (animation === valueAnimator) {
            view()?.isInvisible = true
        }
    }

    override fun onAnimationRepeat(animation: Animator) {
    }

}