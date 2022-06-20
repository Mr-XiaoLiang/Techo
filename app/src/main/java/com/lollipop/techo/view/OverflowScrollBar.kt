package com.lollipop.techo.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.techo.R
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
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

    interface OnScrollChangedListener {
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

        private val barBounds = RectF()

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

        override fun onBoundsChange(bounds: Rect?) {
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