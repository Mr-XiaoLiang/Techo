package com.lollipop.punch2.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import kotlin.math.sqrt

class StampBackgroundDrawable : Drawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    var color: Int
        get() {
            return paint.color
        }
        set(value) {
            paint.color = value
            invalidateSelf()
        }

    var backgroundColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            invalidateSelf()
        }

    var rectHeight = 50
        set(value) {
            field = value
            onBoundChanged()
        }

    var rotation: Float = 30F
        set(value) {
            field = value
            onBoundChanged()
        }

    private val rect = Rect()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        onBoundChanged()
    }

    private fun onBoundChanged() {
        val b = bounds
        if (b.isEmpty) {
            return
        }
        val maxWidth = sqrt(b.width() * b.width() * b.height() * b.height() * 1.0).toInt()
        rect.set(0, 0, maxWidth, rectHeight)
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (backgroundColor != Color.TRANSPARENT) {
            canvas.drawColor(backgroundColor)
        }
        if (bounds.isEmpty) {
            return
        }
        val saveCount = canvas.save()
        canvas.translate(0F, rect.height() * 0.5F * -1)
        canvas.rotate(rotation * -1)
        canvas.drawRect(rect, paint)
        canvas.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }
}