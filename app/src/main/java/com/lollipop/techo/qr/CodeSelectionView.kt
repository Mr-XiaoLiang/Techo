package com.lollipop.techo.qr

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Size
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.qr.BarcodeWrapper
import com.lollipop.techo.R
import kotlin.math.max

class CodeSelectionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    private val boundsDrawable = BoundsDrawable()

    init {
        setImageDrawable(boundsDrawable)
        attributeSet?.let { attr ->
            val typeArray = context.obtainStyledAttributes(attr, R.styleable.CodeSelectionView)
            color = typeArray.getColor(R.styleable.CodeSelectionView_android_color, Color.BLACK)
            radius = typeArray.getDimensionPixelSize(
                R.styleable.CodeSelectionView_android_radius, 0
            ).toFloat()
            strokeWidth = typeArray.getDimensionPixelSize(
                R.styleable.CodeSelectionView_strokeWidth, 0
            ).toFloat()
            spaceWeight = typeArray.getFloat(R.styleable.CodeSelectionView_spaceWeight, 0.5F)
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

    fun onCodeResult(size: Size, list: List<BarcodeWrapper>) {
        val viewWidth = width
        val viewHeight = height
        val srcWidth = size.width
        val srcHeight = size.height
        val weight = max(viewWidth * 1F / srcWidth, viewHeight * 1F / srcHeight)
        val offsetX = (viewWidth - srcWidth * weight) / 2
        val offsetY = (viewHeight - srcHeight * weight) / 2

        val boundsList = list.map { it.describe.boundingBox.copyOf(weight, offsetX, offsetY) }

        boundsDrawable.setRectList(boundsList)
    }

    private fun Rect.copyOf(weight: Float, offsetX: Float, offsetY: Float): Rect {
        val src = this
        return Rect(
            src.left.offset(weight, offsetX),
            src.top.offset(weight, offsetY),
            src.right.offset(weight, offsetX),
            src.bottom.offset(weight, offsetY),
        )
    }

    private fun Int.offset(weight: Float, offset: Float): Int {
        val value = this
        return (value * weight + offset).toInt()
    }

    private class BoundsDrawable : Drawable() {

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

        private val boundsPath = Path()

        private val rectList = ArrayList<Rect>()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            buildPath()
        }

        fun setRectList(list: List<Rect>) {
            rectList.clear()
            rectList.addAll(list)
            buildPath()
            invalidateSelf()
        }

        private fun buildPath() {
            boundsPath.reset()
            if (bounds.isEmpty) {
                return
            }
            rectList.forEach {
                addRect(it, boundsPath)
            }
        }

        /**
         * ⎡ ⎤
         * ⎣ ⎦
         */
        private fun addRect(rect: Rect, path: Path) {
            val width = rect.width() - strokeWidth
            val height = rect.height() - strokeWidth
            val halfStroke = strokeWidth * 0.5F
            val halfRadius = radius * 0.5F
            val left = rect.left + halfStroke
            val top = rect.top + halfStroke
            val right = rect.right - halfStroke
            val bottom = rect.bottom - halfStroke

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
            if (boundsPath.isEmpty) {
                return
            }
            canvas.drawPath(boundsPath, paint)
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