package com.lollipop.faceicon

import android.animation.Animator
import android.animation.ValueAnimator
import java.util.LinkedList

class FaceAnimator(
    private val postNextCallback: (FaceIcon, Float) -> Unit,
    private val progressChangedCallback: (Float) -> Unit
): ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private val pendingFaceIcon = LinkedList<FaceIcon>()

    private val valueAnimator = ValueAnimator()

    var animationDuration: Long
        set(value) {
            valueAnimator.duration = value
        }
        get() {
            return valueAnimator.duration
        }

    private var animationProgress = 0F

    init {
        valueAnimator.addUpdateListener(this)
        valueAnimator.addListener(this)
    }

    fun post(icon: FaceIcon) {
        pendingFaceIcon.addLast(icon)
        start()
    }

    fun end() {
        valueAnimator.end()
    }

    private fun start() {
        if (valueAnimator.isStarted) {
            return
        }
        if (pendingFaceIcon.isEmpty()) {
            return
        }
        val first = pendingFaceIcon.removeFirst()
        animationProgress = 0F
        postNextCallback(first, animationProgress)
        valueAnimator.setFloatValues(0F, 1F)
        valueAnimator.start()
    }

    private fun onProgressChanged(progress: Float) {
        this.animationProgress = progress
        progressChangedCallback(progress)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        if (animation === valueAnimator) {
            val value = animation.animatedValue
            if (value is Float) {
                onProgressChanged(value)
            }
        }
    }

    override fun onAnimationStart(animation: Animator) {
        // 开始了，但是感觉不需要
    }

    override fun onAnimationEnd(animation: Animator) {
        // 停止了，说明结束了
        if (animation === valueAnimator) {
            // 结束了，就开启下一个任务
            start()
        }
    }

    override fun onAnimationCancel(animation: Animator) {
        // 被取消了
    }

    override fun onAnimationRepeat(animation: Animator) {
        // 重播暂时不需要
    }


}