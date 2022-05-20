package com.lollipop.techo.split

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

abstract class SplitDrawable(protected val context: Context) : Drawable() {

    protected val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
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