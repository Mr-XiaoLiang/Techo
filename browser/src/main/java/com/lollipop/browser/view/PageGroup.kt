package com.lollipop.browser.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.OverScroller
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.SingleTouchSlideHelper
import com.lollipop.base.util.log
import kotlin.math.abs

class PageGroup @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null
) : ViewGroup(context, attributeSet),
    SingleTouchSlideHelper.OnTouchOffsetListener,
    SingleTouchSlideHelper.OnTouchEndListener,
    SingleTouchSlideHelper.OnClickListener {

    var previewInterval = 0
    var previewScale = 0.6F

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

    private val zoomHelper by lazy {
        ZoomHelper().onStart(::onZoomAnimationStart)
            .onEnd(::onZoomAnimationEnd)
            .onUpdate(::onZoomAnimationUpdate)
    }

    private val zoomInInterpolator = OvershootInterpolator()
    private val zoomOutInterpolator = AccelerateInterpolator()

    private var pagePosition = 0
    private var pageOffset = 0

    private val singleTouchSlideHelper = SingleTouchSlideHelper(
        context, SingleTouchSlideHelper.Slider.Horizontally
    )

    private val overScroller = OverScroller(context)
    private var velocityTracker: VelocityTracker? = null
    private val minimumVelocity: Int
    private val maximumVelocity: Int
    private var lastScrollX = 0

    private val pageClickListener = ListenerManager<OnPageClickListener>()

    init {
        singleTouchSlideHelper.addEndListener(this)
        singleTouchSlideHelper.addOffsetListener(this)
        singleTouchSlideHelper.addClickListener(this)
        val configuration = ViewConfiguration.get(context)
        minimumVelocity = configuration.scaledMinimumFlingVelocity
        maximumVelocity = configuration.scaledMaximumFlingVelocity
    }

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
        pageWidth = width - paddingLeft - paddingRight
        pageHeight = height - paddingTop - paddingBottom
        layoutPage()
    }

    private fun layoutPage() {
        var childLeft = paddingLeft
        val childTop = paddingTop
        val spaceWidth = if (isPreviewMode) {
            (pageWidth * previewScale + previewInterval).toInt()
        } else {
            pageWidth
        }

        val childWidth: Int
        val childHeight: Int

        // 偏移到第一个的位置
        childLeft -= pagePosition * spaceWidth
        // 加上偏移到中间
        if (isPreviewMode) {
            childWidth = (pageWidth * previewScale).toInt()
            childHeight = (pageHeight * previewScale).toInt()
            childLeft += ((pageWidth - childWidth) * 0.5F).toInt()
        } else {
            childWidth = pageWidth
            childHeight = pageHeight
        }

        childLeft -= pageOffset

        for (i in 0 until childCount) {
            getChildAt(i)?.layout(
                childLeft,
                childTop,
                childLeft + childWidth,
                childTop + childHeight
            )
            childLeft += spaceWidth
        }

    }

    private fun scrollPage(position: Int, offset: Int, preview: Boolean = isPreviewMode) {
        log("scrollPage: $position, $offset")
        pagePosition = position
        pageOffset = offset

        val childTop = paddingTop
        var childLeft = paddingLeft
        val spaceWidth = if (preview) {
            (pageWidth * previewScale + previewInterval).toInt()
        } else {
            pageWidth
        }
        // 偏移到第一个的位置
        childLeft -= position * spaceWidth
        childLeft -= offset
        for (i in 0 until childCount) {
            getChildAt(i)?.layout(childLeft, childTop, childLeft + pageWidth, childTop + pageHeight)
            childLeft += spaceWidth
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (zoomHelper.isRunning) {
            // 动画过程中不能操作
            return true
        }
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initOrResetVelocityTracker()
                velocityTracker?.addMovement(ev)
                if (isPreviewMode) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                initVelocityTrackerIfNotExists()
                velocityTracker?.addMovement(ev)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (zoomHelper.isRunning) {
            // 动画过程中不能操作
            return true
        }
        if (!isPreviewMode) {
            return super.onTouchEvent(event)
        }
        initVelocityTrackerIfNotExists()
        velocityTracker?.addMovement(event)
        return singleTouchSlideHelper.onTouch(event)
    }

    private fun initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker?.clear()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    override fun computeScroll() {
        super.computeScroll()
        if (overScroller.computeScrollOffset()) {
            val currX = overScroller.currX
            val offsetX = currX - lastScrollX
            lastScrollX = currX
            onTouchMoved(offsetX.toFloat(), 0F)
            postInvalidateOnAnimation()
        } else if (pageOffset != 0) {
            springBack()
        }
    }

    private fun springBack() {
        val spaceWidth = if (isPreviewMode) {
            (pageWidth * previewScale + previewInterval).toInt()
        } else {
            pageWidth
        }
        val offset = pageOffset
        val targetOffset = if (offset > (spaceWidth / 2) && pagePosition < (childCount - 1)) {
            spaceWidth
        } else {
            0
        }
        overScroller.abortAnimation()
        overScroller.startScroll(offset, 0, targetOffset, 0)
        postInvalidateOnAnimation()
    }

    fun setMode(preview: Boolean) {
        isPreviewMode = preview
        zoomHelper.start(preview)
    }

    private fun onZoomAnimationStart() {
        scrollPage(pagePosition, 0, false)
    }

    private fun onZoomAnimationEnd() {
        for (i in 0 until childCount) {
            getChildAt(i)?.let {
                it.translationX = 0F
            }
        }
        scrollPage(pagePosition, 0)
    }

    private fun onZoomAnimationUpdate(p: Float) {
        val progress = if (isPreviewMode) {
            zoomInInterpolator.getInterpolation(1 - p)
        } else {
            zoomOutInterpolator.getInterpolation(1 - p)
        }
        val scale = (1F - previewScale) * (1 - progress) + previewScale
        val spaceWidth = pageWidth * previewScale + previewInterval
        val pageOffset = (pageWidth - spaceWidth) * progress
        val currentPosition = pagePosition
        for (i in 0 until childCount) {
            getChildAt(i)?.let {
                it.scaleX = scale
                it.scaleY = scale
                it.translationX = (pageOffset * (currentPosition - i))
            }
        }
    }

    private fun getChildLeft(selected: Int, position: Int, preview: Boolean): Int {
        val positionOffset = position - selected
        return if (preview) {
            (positionOffset * ((pageWidth * previewScale) + previewInterval)).toInt()
        } else {
            positionOffset * pageWidth
        }
    }

    override fun onTouchMoved(offsetX: Float, offsetY: Float) {
        log("onTouchMoved: $offsetX")
        if (!overScroller.isFinished) {
            overScroller.abortAnimation()
        }
        var newOffset = (pageOffset - offsetX).toInt()
        var page = pagePosition
        val spaceWidth = if (isPreviewMode) {
            (pageWidth * previewScale + previewInterval).toInt()
        } else {
            pageWidth
        }
        if (newOffset < 0) {
            if (page > 0) {
                page--
                newOffset += spaceWidth
            } else {
                newOffset = 0
            }
        } else if (newOffset > 0) {
            if (page >= childCount - 1) {
                newOffset = 0
            } else if (newOffset > spaceWidth) {
                page++
                newOffset -= spaceWidth
            }
        }
        scrollPage(page, newOffset)
    }

    override fun onTouchEnd(isCancel: Boolean) {
        val spaceWidth = if (isPreviewMode) {
            (pageWidth * previewScale + previewInterval).toInt()
        } else {
            pageWidth
        }
        val currentOffset = pageOffset
        overScroller.abortAnimation()
        val xVelocity = velocityTracker?.xVelocity ?: 0F
        recycleVelocityTracker()
        val maxOffset = (childCount - pagePosition) * spaceWidth - currentOffset
        val minOffset = (pagePosition * spaceWidth + currentOffset) * -1
        overScroller.fling(
            currentOffset, 0,
            xVelocity.toInt(), 0,
            minOffset, maxOffset,
            0, 0
        )
        lastScrollX = currentOffset
        postInvalidateOnAnimation()
    }

    override fun onClick(x: Float, y: Float) {
        val xInt = x.toInt()
        val yInt = y.toInt()

        val position = pagePosition
        val offset = pageOffset
        var childLeft = paddingLeft
        var childTop = paddingTop
        val spaceWidth = if (isPreviewMode) {
            (pageWidth * previewScale + previewInterval).toInt()
        } else {
            pageWidth
        }
        // 偏移到第一个的位置
        childLeft -= position * spaceWidth
        val cardWidth = if (isPreviewMode) {
            (pageWidth * previewScale).toInt()
        } else {
            pageWidth
        }
        val cardHeight = if (isPreviewMode) {
            (pageHeight * previewScale).toInt()
        } else {
            pageHeight
        }
        // 加上偏移到中间
        if (isPreviewMode) {
            childLeft += ((pageWidth - cardWidth) * 0.5F).toInt()
            childTop += ((pageHeight - cardHeight) * 0.5F).toInt()
        }
        childLeft -= offset
        val tempRect = Rect()

        for (i in 0 until childCount) {
            tempRect.set(childLeft, childTop, childLeft + cardWidth, childTop + cardHeight)
            if (tempRect.contains(xInt, yInt)) {
                onPageClick(i)
                return
            }
            childLeft += spaceWidth
        }
    }

    fun addPageClickListener(listener: OnPageClickListener) {
        pageClickListener.addListener(listener)
    }

    fun removePageClickListener(listener: OnPageClickListener) {
        pageClickListener.removeListener(listener)
    }

    private fun onPageClick(position: Int) {
        pageClickListener.invoke { it.onPageClick(position) }
    }

    fun interface OnPageClickListener {
        fun onPageClick(position: Int)
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

        val isRunning: Boolean
            get() {
                return zoomAnimator.isRunning
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