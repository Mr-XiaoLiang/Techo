package com.lollipop.techo.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import androidx.annotation.ColorInt
import com.lollipop.base.graphics.LDrawable

class MosaicDrawable : LDrawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    private var lightColor = Color.WHITE
    private var darkColor = Color.BLACK
    private var stepLength = 0
    private var mosaicPath = Path()
    private var alpha = 255

    fun setStep(step: Int): MosaicDrawable {
        this.stepLength = step
        return this
    }

    fun setColor(@ColorInt light: Int, @ColorInt dark: Int): MosaicDrawable {
        this.lightColor = light
        this.darkColor = dark
        invalidateSelf()
        return this
    }

    private fun buildPath() {
        val path = mosaicPath
        path.reset()
        val step = stepLength
        if (step < 1) {
            return
        }
        val b = bounds
        val width = b.width()
        val height = b.height()
        val leftEdge = b.left
        val topEdge = b.top
        if (width < 1 || height < 1) {
            return
        }
        val xCount = width / step + 1
        val yCount = height / step + 1
        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                if ((x + y) % 2 == 0) {
                    // 要跳格子
                    continue
                }
                val left = (x * step + leftEdge).toFloat()
                val top = (y * step + topEdge).toFloat()
                path.addRect(left, top, left + step, top + step, Path.Direction.CW)
            }
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        buildPath()
    }

    override fun draw(canvas: Canvas) {
        if (stepLength < 1 || mosaicPath.isEmpty) {
            return
        }
        paint.color = lightColor
        paint.alpha = alpha
        canvas.drawPaint(paint)

        paint.color = darkColor
        paint.alpha = alpha
        canvas.drawPath(mosaicPath, paint)
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

}