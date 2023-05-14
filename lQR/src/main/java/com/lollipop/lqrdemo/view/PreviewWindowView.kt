package com.lollipop.lqrdemo.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.lqrdemo.R

class PreviewWindowView @JvmOverloads constructor(
    context: Context, attr: AttributeSet? = null
) : AppCompatImageView(context, attr) {

    private val maskDrawable = MaskDrawable()

    init {
        background = maskDrawable
        attr?.let {
            val typeArray = context.obtainStyledAttributes(attr, R.styleable.PreviewWindowView)
            radius = typeArray.getDimensionPixelSize(
                R.styleable.PreviewWindowView_android_radius, 0
            ).toFloat()
            color = typeArray.getColor(R.styleable.PreviewWindowView_color, Color.WHITE)
            typeArray.recycle()
        }
        maskDrawable.paddingRect.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        maskDrawable.paddingRect.set(left, top, right, bottom)
        maskDrawable.resetPath()
    }

    var radius: Float
        get() {
            return maskDrawable.radius
        }
        set(value) {
            maskDrawable.radius = value
        }

    var color: Int
        get() {
            return maskDrawable.color
        }
        set(value) {
            maskDrawable.color = value
            maskDrawable.invalidateSelf()
        }

    private class MaskDrawable : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
        }

        var radius: Float = 0F

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        val paddingRect = Rect()

        private val maskPath = Path()
        private val windowPath = Path()

        private val boundsF = RectF()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            boundsF.set(bounds)
            resetPath()
        }

        fun resetPath() {
            if (boundsF.isEmpty) {
                return
            }
            maskPath.reset()
            windowPath.reset()
            maskPath.addRect(boundsF, Path.Direction.CW)
            windowPath.addRoundRect(
                boundsF.left + paddingRect.left,
                boundsF.top + paddingRect.top,
                boundsF.right - paddingRect.right,
                boundsF.bottom - paddingRect.bottom,
                radius, radius,
                Path.Direction.CW
            )
            maskPath.op(windowPath, Path.Op.DIFFERENCE)
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawPath(maskPath, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
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