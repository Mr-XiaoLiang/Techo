package com.lollipop.techo.drawable

import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import com.lollipop.base.util.range

class CircularProgressDrawable : Drawable(), Animatable {

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    val padding = Rect()

    var strokeWidth = 1F

    var color: Int
        get() {
            return paint.color
        }
        set(value) {
            paint.color = value
        }

    var progress = 0F
        set(value) {
            field = value
            invalidateSelf()
        }

    private val arcBounds = RectF()

    private var degrees = 0F

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.rotate(degrees, bounds.exactCenterX(), bounds.exactCenterY())
        val sweepAngle = progress.range(0F, 1F) * 360
        canvas.drawArc(arcBounds, 0F, sweepAngle, false, paint)
        canvas.restoreToCount(saveCount)
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

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun isRunning(): Boolean {
        TODO("Not yet implemented")
    }
}