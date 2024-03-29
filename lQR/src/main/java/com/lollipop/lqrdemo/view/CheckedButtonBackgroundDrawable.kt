package com.lollipop.lqrdemo.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.lollipop.base.graphics.LDrawable
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.dp2px
import kotlin.math.min

class CheckedButtonBackgroundDrawable : LDrawable() {

    companion object {
        var BORDER_WIDTH = 1F
        var FILL_ALPHA = 0.3F
    }

    private var radius = 0F
    private val borderWidth = BORDER_WIDTH.dp2px
    private val drawBounds = RectF()

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    private var drawableAlpha = 255

    var color: Int = Color.BLACK
        set(value) {
            field = value
            updateColor()
        }

    private var bgColor = color
    private var borderColor = color

    private fun updateColor() {
        val value = color
        val baseColor = value.changeAlpha(drawableAlpha)
        bgColor = baseColor.changeAlpha(FILL_ALPHA)
        borderColor = baseColor
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        radius = min(bounds.width(), bounds.height()) * 0.5F
        val halfBorder = borderWidth / 2
        drawBounds.set(
            bounds.left + halfBorder,
            bounds.top + halfBorder,
            bounds.right - halfBorder,
            bounds.bottom - halfBorder,
        )
    }

    override fun draw(canvas: Canvas) {
        paint.setColor(bgColor)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(
            drawBounds,
            radius,
            radius,
            paint
        )
        paint.setColor(borderColor)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(
            drawBounds,
            radius,
            radius,
            paint
        )
    }

    override fun setAlpha(alpha: Int) {
        drawableAlpha = alpha
        updateColor()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

}