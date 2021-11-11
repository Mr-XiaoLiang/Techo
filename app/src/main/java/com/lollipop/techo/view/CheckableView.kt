package com.lollipop.techo.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sqrt

/**
 * @author lollipop
 * @date 2021/11/11 22:39
 */
class CheckableView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private class RippleAnimationDrawableWrapper(
        private val realDrawable: Drawable
    ) : Drawable(), Drawable.Callback {

        init {
            this.callback = realDrawable.callback
            realDrawable.callback = this
        }

        private var radius = 0F

        private val clipPath = Path()

        private val clipBounds = RectF()

        var rippleProgress = 1F
            set(value) {
                field = value
                checkPath()
            }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            val w = bounds.width() * 0.5
            val h = bounds.height() * 0.5
            radius = sqrt(w * w + h * h).toFloat()
        }

        private fun checkPath() {
            if (bounds.isEmpty) {
                return
            }
            val fl = radius * rippleProgress
            val exactCenterX = bounds.exactCenterX()
            val exactCenterY = bounds.exactCenterY()
            clipBounds.set(
                exactCenterX - fl,
                exactCenterY - fl,
                exactCenterX + fl,
                exactCenterY + fl
            )
            clipPath.reset()
            clipPath.addOval(clipBounds, Path.Direction.CW)
            invalidateSelf()
        }

        override fun setChangingConfigurations(configs: Int) {
            super.setChangingConfigurations(configs)
            realDrawable.changingConfigurations = configs
        }

        override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
            super.setBounds(left, top, right, bottom)
            realDrawable.setBounds(left, top, right, bottom)
        }

        override fun draw(canvas: Canvas) {
            if (bounds.isEmpty) {
                return
            }
            when {
                rippleProgress > 1 -> {
                    realDrawable.draw(canvas)
                }
                rippleProgress > 0 -> {
                    val saveCount = canvas.save()
                    canvas.clipPath(clipPath)
                    realDrawable.draw(canvas)
                    canvas.restoreToCount(saveCount)
                }
            }
        }

        override fun setAlpha(alpha: Int) {
            realDrawable.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            realDrawable.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return realDrawable.opacity
        }

        override fun invalidateDrawable(who: Drawable) {
            this.callback?.invalidateDrawable(this)
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, whenTime: Long) {
            this.callback?.scheduleDrawable(this, what, whenTime)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            this.callback?.unscheduleDrawable(this, what)
        }
    }

}