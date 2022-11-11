package com.lollipop.techo.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CameraFocusBoundsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {


    private class CameraFocusBoundsDrawable : Drawable() {

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

        var strokeWidth: Float
            get() {
                return paint.strokeWidth
            }
            set(value) {
                paint.strokeWidth = value
            }

        var radius = 0F

        var spaceWeight = 0.5F

        override fun draw(canvas: Canvas) {
            TODO("Not yet implemented")
        }

        override fun setAlpha(alpha: Int) {
            TODO("Not yet implemented")
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            TODO("Not yet implemented")
        }

        override fun getOpacity(): Int {
            TODO("Not yet implemented")
        }

    }

}