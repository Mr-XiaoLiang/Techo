package com.lollipop.browser.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class WebProgressBar @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    private val progressDrawable = ProgressDrawable()

    init {
        setImageDrawable(progressDrawable)
    }

    private class ProgressDrawable : Drawable() {

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