package com.lollipop.qr.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.qr.R

class CameraFocusBoundsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    private val boundsDrawable = CameraFocusBoundsDrawable()

    init {
        setImageDrawable(boundsDrawable)
        attributeSet?.let { attr ->
            val typeArray = context.obtainStyledAttributes(attr, R.styleable.CameraFocusBoundsView)
            color = typeArray.getColor(R.styleable.CameraFocusBoundsView_android_color, Color.BLACK)
            radius = typeArray.getDimensionPixelSize(
                R.styleable.CameraFocusBoundsView_android_radius, 0
            ).toFloat()
            strokeWidth = typeArray.getDimensionPixelSize(
                R.styleable.CameraFocusBoundsView_strokeWidth, 0
            ).toFloat()
            spaceWeight = typeArray.getFloat(R.styleable.CameraFocusBoundsView_spaceWeight, 0.5F)
            typeArray.recycle()
        }
    }

    var color: Int
        get() {
            return boundsDrawable.color
        }
        set(value) {
            boundsDrawable.color = value
        }

    var strokeWidth: Float
        get() {
            return boundsDrawable.strokeWidth
        }
        set(value) {
            boundsDrawable.strokeWidth = value
        }

    var radius: Float
        get() {
            return boundsDrawable.radius
        }
        set(value) {
            boundsDrawable.radius = value
        }

    var spaceWeight: Float
        get() {
            return boundsDrawable.spaceWeight
        }
        set(value) {
            boundsDrawable.spaceWeight = value
        }

    private class CameraFocusBoundsDrawable : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
        }

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        var strokeWidth: Float
            get() {
                return paint.strokeWidth
            }
            set(value) {
                paint.strokeWidth = value
            }

        var radius = 0F

        var spaceWeight = 0.5F

        private val path = Path()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            buildPath()
        }

        /**
         * ⎡ ⎤
         * ⎣ ⎦
         */
        private fun buildPath() {
            path.reset()
            if (bounds.isEmpty) {
                return
            }
            val width = bounds.width() - strokeWidth
            val height = bounds.height() - strokeWidth
            val halfStroke = strokeWidth * 0.5F
            val halfRadius = radius * 0.5F
            val left = bounds.left + halfStroke
            val top = bounds.top + halfStroke
            val right = bounds.right - halfStroke
            val bottom = bounds.bottom - halfStroke

            val vLength = height * (1 - spaceWeight) / 2 - radius
            val hLength = width * (1 - spaceWeight) / 2 - radius

            // 左上角
            path.moveTo(left, top + vLength + radius)
            path.lineTo(left, top + radius)
            path.cubicTo(
                left, top + halfRadius,
                left + halfRadius, top,
                left + radius, top
            )
            path.lineTo(left + radius + hLength, top)

            // 右上角
            path.moveTo(right - radius - hLength, top)
            path.lineTo(right - radius, top)
            path.cubicTo(right - halfRadius, top, right, top + halfRadius, right, top + radius)
            path.lineTo(right, top + radius + vLength)

            // 右下角
            path.moveTo(right, bottom - radius - vLength)
            path.lineTo(right, bottom - radius)
            path.cubicTo(
                right, bottom - halfRadius,
                right - halfRadius, bottom,
                right - radius, bottom
            )
            path.lineTo(right - radius - hLength, bottom)

            // 左下角
            path.moveTo(left + radius + hLength, bottom)
            path.lineTo(left + radius, bottom)
            path.cubicTo(
                left + halfRadius, bottom,
                left, bottom - halfRadius,
                left, bottom - radius
            )
            path.lineTo(left, bottom - radius - vLength)
        }

        override fun draw(canvas: Canvas) {
            if (path.isEmpty) {
                return
            }
            canvas.drawPath(path, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

}