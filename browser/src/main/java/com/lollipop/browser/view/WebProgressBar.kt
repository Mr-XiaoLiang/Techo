package com.lollipop.browser.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.graphics.LDrawable

class WebProgressBar @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    private val progressDrawable = ProgressDrawable()

    var color: Int
        get() {
            return progressDrawable.color
        }
        set(value) {
            progressDrawable.color = value
        }

    var progress: Float
        set(value) {
            progressDrawable.progress = value
        }
        get() {
            return progressDrawable.progress
        }

    init {
        setImageDrawable(progressDrawable)
        if (isInEditMode) {
            color = 0x30FF0000
            progress = 0.3F
        }
    }

    private class ProgressDrawable : LDrawable() {

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
            }

        var progress = 0F
            set(value) {
                field = value
                buildProgress()
            }

        private val progressRect = Rect()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            buildProgress()
        }

        private fun buildProgress() {
            val width = (bounds.width() * progress).toInt()
            progressRect.set(bounds.left, bounds.top, bounds.left + width, bounds.bottom)
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRect(progressRect, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

    }

}