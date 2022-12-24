package com.lollipop.browser.view

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.abs

class PageGroup @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null
) : ViewGroup(context, attributeSet) {

    var previewInterval = 0
    var previewScale = 0.8F

    var animationDuration: Long
        get() {
            return zoomHelper.animationDuration
        }
        set(value) {
            zoomHelper.animationDuration = value
        }

    private var isPreviewMode = false

    private var pageWidth = 0
    private var pageHeight = 0
    private var previewOffsetX = 0F

    private val zoomHelper by lazy {
        ZoomHelper().onStart(::onZoomAnimationStart)
            .onEnd(::onZoomAnimationEnd)
            .onUpdate(::onZoomAnimationUpdate)
    }

    private val zoomInInterpolator = OvershootInterpolator()
    private val zoomOutInterpolator = AccelerateInterpolator()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSize = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val heightSize = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val childWidthSpec = MeasureSpec.makeMeasureSpec(
            widthSize - paddingLeft - paddingRight,
            MeasureSpec.EXACTLY
        )
        val childHeightSpec = MeasureSpec.makeMeasureSpec(
            heightSize - paddingTop - paddingBottom,
            MeasureSpec.EXACTLY
        )

        for (i in 0 until childCount) {
            getChildAt(i)?.measure(childWidthSpec, childHeightSpec)
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        val widthSize = width
        val heightSize = height

        var childLeft = paddingLeft
        val childTop = paddingTop

        val childWidth = widthSize - paddingLeft - paddingRight
        val childHeight = heightSize - paddingTop - paddingBottom
        pageWidth = childWidth
        pageHeight = childHeight
        previewOffsetX = pageWidth * (1 - previewScale) * -1 + previewInterval

        for (i in 0 until childCount) {
            getChildAt(i)?.layout(
                childLeft,
                childTop,
                childLeft + childWidth,
                childTop + childHeight
            )
            childLeft += childWidth
            if (isPreviewMode) {
                childLeft += previewInterval
            }
        }
    }

    fun setMode(preview: Boolean) {
        isPreviewMode = preview
        zoomHelper.start(preview)
    }

    fun setInterpolator(interpolator: TimeInterpolator) {
        zoomHelper.setInterpolator(interpolator)
    }

    private fun onZoomAnimationStart() {

    }

    private fun onZoomAnimationEnd() {

    }

    private fun onZoomAnimationUpdate(p: Float) {
        val progress = if (isPreviewMode) {
            zoomInInterpolator.getInterpolation(1 - p)
        } else {
            zoomOutInterpolator.getInterpolation(1 - p)
        }
        val scale = (1F - previewScale) * (1 - progress) + previewScale
        for (i in 0 until childCount) {
            getChildAt(i)?.let {
                it.scaleX = scale
                it.scaleY = scale
                it.translationX = (previewOffsetX * i) * progress
            }
        }
    }

    private class ZoomHelper : ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private val zoomAnimator = ValueAnimator().apply {
            addUpdateListener(this@ZoomHelper)
            addListener(this@ZoomHelper)
        }

        /**
         * 缩放子 View 的时候的进度值
         * 1F: 表示完全展开的状态
         * 0F: 表示完全缩小后的状态
         */
        var zoomProgress = 1F
        var animationDuration = 0L
        private var onUpdateListener: ((Float) -> Unit)? = null
        private var onStartListener: (() -> Unit)? = null
        private var onEndListener: (() -> Unit)? = null
        var zoomOutValue = 1F
        var zoomInValue = 0F

        var selectedPage = 0

        fun setInterpolator(interpolator: TimeInterpolator) {
            zoomAnimator.interpolator = interpolator
        }

        fun onUpdate(listener: (Float) -> Unit): ZoomHelper {
            onUpdateListener = listener
            return this
        }

        fun onStart(listener: () -> Unit): ZoomHelper {
            onStartListener = listener
            return this
        }

        fun onEnd(listener: () -> Unit): ZoomHelper {
            onEndListener = listener
            return this
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            if (animation == zoomAnimator) {
                zoomProgress = animation.animatedValue as Float
                onUpdateListener?.invoke(zoomProgress)
            }
        }

        override fun onAnimationRepeat(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            onEndListener?.invoke()
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationStart(animation: Animator) {
            onStartListener?.invoke()
        }

        fun start(zoomIn: Boolean) {
            zoomAnimator.cancel()
            val target = if (zoomIn) {
                zoomInValue
            } else {
                zoomOutValue
            }
            zoomAnimator.setFloatValues(zoomProgress, target)
            val durationWeight = abs(zoomProgress - target) / abs(zoomOutValue - zoomInValue)
            zoomAnimator.duration = (animationDuration * 1F * durationWeight).toLong()
            zoomAnimator.start()
        }

        fun end() {
            zoomAnimator.end()
        }
    }

}