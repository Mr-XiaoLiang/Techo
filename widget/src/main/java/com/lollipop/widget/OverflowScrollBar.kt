package com.lollipop.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.lazyLogD
import kotlin.math.max
import kotlin.math.min

class OverflowScrollBar(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private val progressDrawable = ProgressDrawable()

    var color: Int
        get() {
            return progressDrawable.color
        }
        set(value) {
            progressDrawable.color = value
        }

    var isHorizontal: Boolean
        set(value) {
            progressDrawable.isHorizontal = value
        }
        get() {
            return progressDrawable.isHorizontal
        }

    var contentWeight: Float
        set(value) {
            progressDrawable.contentWeight = value
        }
        get() {
            return progressDrawable.contentWeight
        }

    var progress: Float
        set(value) {
            progressDrawable.progress = value
        }
        get() {
            return progressDrawable.progress
        }

    private val onScrollChangedListener = ArrayList<OnScrollChangedListener>()

    private var lastTouchPoint = PointF()

    private var activeTouchId = 0

    private val barRange: RectF
        get() {
            return progressDrawable.barBounds
        }

    private val log by lazyLogD()

    init {
        setImageDrawable(progressDrawable)
        attributeSet?.let { attrs ->
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.OverflowScrollBar)
            color = typeArray.getColor(R.styleable.OverflowScrollBar_android_color, Color.BLACK)
            isHorizontal = typeArray.getBoolean(R.styleable.OverflowScrollBar_isHorizontal, false)
            typeArray.recycle()
        }
        if (isInEditMode) {
            contentWeight = 0.5F
            progress = 0.3F
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onTouchDown(event)
            }
            else -> {
                onTouchMove(event)
            }
        }
    }

    private fun onTouchDown(event: MotionEvent): Boolean {
        activeTouchId = event.getPointerId(0)
        val x = event.getX(0).limit(width.toFloat())
        val y = event.getY(0).limit(height.toFloat())

        if (isHorizontal) {
            if (x >= barRange.left && x <= barRange.right) {
                lastTouchPoint.set(x, y)
                return true
            }
        } else {
            if (y >= barRange.top && y <= barRange.bottom) {
                lastTouchPoint.set(x, y)
                return true
            }
        }
        return false
    }

    private fun Float.limit(max: Float): Float {
        if (this < 0) {
            return 0F
        }
        if (this > max) {
            return max
        }
        return this
    }

    private fun onTouchMove(event: MotionEvent): Boolean {
        val index = event.findPointerIndex(activeTouchId)
        if (index < 0) {
            return onTouchDown(event)
        }
        val x = event.getX(index).limit(width.toFloat())
        val y = event.getY(index).limit(height.toFloat())
        if (isHorizontal) {
            val offsetX = x - lastTouchPoint.x
            offsetByHorizontal(offsetX)
        } else {
            val offsetY = y - lastTouchPoint.y
            offsetByVertical(offsetY)
        }
        log("onTouchMove: $x, $y")
        lastTouchPoint.set(x, y)
        return true
    }

    private fun offsetByHorizontal(offset: Float) {
        val contentWidth = width - paddingLeft - paddingRight
        val offsetMax = contentWidth * (1 - contentWeight)
        val newProgress = offset / offsetMax + progress
        dispatchScrollChanged(newProgress)
    }

    private fun offsetByVertical(offset: Float) {
        val contentHeight = height - paddingTop - paddingBottom
        val offsetMax = contentHeight * (1 - contentWeight)
        val newProgress = offset / offsetMax + progress
        log("progress: $progress")
        dispatchScrollChanged(newProgress)
    }

    private fun dispatchScrollChanged(p: Float) {
        progress = p
        post {
            onScrollChangedListener.forEach {
                it.onScrollChanged(p)
            }
        }
    }

    fun addListener(listener: OnScrollChangedListener) {
        onScrollChangedListener.add(listener)
    }

    fun removeListener(listener: OnScrollChangedListener) {
        onScrollChangedListener.remove(listener)
    }

    fun interface OnScrollChangedListener {
        fun onScrollChanged(progress: Float)
    }

    private class ProgressDrawable : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
        }

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        var isHorizontal = false
            set(value) {
                field = value
                onBarChanged()
            }

        var contentWeight = 1F
            set(value) {
                field = max(0F, min(1F, value))
                onBarChanged()
            }

        var progress = 1F
            set(value) {
                field = max(0F, min(1F, value))
                onBarChanged()
            }

        val barBounds = RectF()

        private var radius = 0F

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(barBounds, radius, radius, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            onBarChanged()
        }

        private fun onBarChanged() {
            if (bounds.isEmpty) {
                return
            }
            val minLength = min(bounds.width(), bounds.height())
            radius = minLength * 0.5F
            if (isHorizontal) {
                barBounds.set(
                    bounds.left.toFloat(),
                    bounds.top.toFloat(),
                    bounds.width() * contentWeight + bounds.left.toFloat(),
                    bounds.bottom.toFloat()
                )
                val offset = (bounds.width() * (1 - contentWeight) * progress)
                barBounds.offset(offset, 0F)
            } else {
                barBounds.set(
                    bounds.left.toFloat(),
                    bounds.top.toFloat(),
                    bounds.right.toFloat(),
                    bounds.height() * contentWeight + bounds.top.toFloat()
                )
                val offset = (bounds.height() * (1 - contentWeight) * progress)
                barBounds.offset(0F, offset)
            }
            invalidateSelf()
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

}