package com.lollipop.techo.drawable

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import com.lollipop.base.util.range
import kotlin.math.min

class CircularProgressDrawable : Drawable(), Animatable, ValueAnimator.AnimatorUpdateListener {

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

    var animationDuration = 300L

    private val rotateAnimator by lazy {
        ValueAnimator().apply {
            addUpdateListener(this@CircularProgressDrawable)
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val width = bounds.width() - padding.left - padding.right - strokeWidth
        val height = bounds.height() - padding.top - padding.bottom - strokeWidth
        val d = min(width, height)
        val r = d / 2
        val left = bounds.centerX() - r
        val top = bounds.centerY() - r
        arcBounds.set(left, top, left + d, top + d)
    }

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
        rotateAnimator.duration = animationDuration
        rotateAnimator.repeatCount = ValueAnimator.INFINITE
        rotateAnimator.repeatMode = ValueAnimator.RESTART
        rotateAnimator.setFloatValues(0F, 360F)
        rotateAnimator.start()
    }

    override fun stop() {
        rotateAnimator.cancel()
    }

    override fun isRunning(): Boolean {
        return rotateAnimator.isRunning
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == rotateAnimator) {
            degrees = animation.animatedValue as Float
            invalidateSelf()
        }
    }
}