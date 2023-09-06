package com.lollipop.lqrdemo.creator

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

object QrCornerSettingHelper {

    fun cutModeDrawable(): ModeDrawable {
        return CutModeDrawable()
    }

    fun roundModeDrawable(): ModeDrawable {
        return RoundModeDrawable()
    }

    fun squircleModeDrawable(): ModeDrawable {
        return SquircleModeDrawable()
    }


    class CutModeDrawable : ModeDrawable() {

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            val height = bounds.height()
            val width = bounds.width()
            val thirdHeight = height * 0.3F
            val thirdWidth = width * 0.3F
            path.reset()
            path.moveTo(bounds.left.toFloat(), thirdHeight)
            path.lineTo(bounds.left + thirdWidth, thirdHeight)
            path.lineTo(bounds.right - thirdWidth, bounds.bottom - thirdHeight)
            path.lineTo(bounds.right - thirdWidth, bounds.bottom.toFloat())
        }

    }

    class RoundModeDrawable : ModeDrawable() {
        override fun draw(canvas: Canvas) {
            val height = bounds.height()
            val width = bounds.width()
            val thirdHeight = height * 0.3F
            val thirdWidth = width * 0.3F
            path.reset()
            path.moveTo(bounds.left.toFloat(), thirdHeight)
            path.lineTo(bounds.left + thirdWidth, thirdHeight)
            path.cubicTo(
                bounds.left + (thirdWidth * 2), thirdHeight,
                bounds.right - thirdWidth, thirdHeight + thirdHeight,
                bounds.right - thirdWidth, bounds.bottom - thirdHeight
            )
            path.lineTo(bounds.right - thirdWidth, bounds.bottom.toFloat())
        }

    }

    class SquircleModeDrawable : ModeDrawable() {
        override fun draw(canvas: Canvas) {
            val height = bounds.height()
            val width = bounds.width()
            val thirdHeight = height * 0.3F
            val thirdWidth = width * 0.3F
            path.reset()
            path.moveTo(bounds.left.toFloat(), thirdHeight)
            path.cubicTo(
                ((width - thirdWidth) * 0.8F) + bounds.left, thirdHeight,
                bounds.right - thirdWidth, bounds.bottom - ((height - thirdHeight) * 0.8F),
                bounds.right - thirdWidth, bounds.bottom.toFloat()
            )
        }

    }


    abstract class ModeDrawable : Drawable() {

        protected val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
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

        protected val path = Path()

        override fun draw(canvas: Canvas) {
            canvas.drawPaint(paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(filter: ColorFilter?) {
            paint.colorFilter = filter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }


    }

}