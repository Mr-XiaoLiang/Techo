package com.lollipop.lqrdemo.writer.background

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt

class ColorBackgroundWriterLayer : BackgroundWriterLayer() {

    companion object {

        var color = Color.WHITE

    }

    private var currentColor = 0
    private var customColor = false

    private val paint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val c = if (customColor) {
            currentColor
        } else {
            color
        }
        if (clipPath.isEmpty) {
            canvas.drawColor(c)
        } else {
            paint.color = c
            canvas.drawPath(clipPath, paint)
        }
    }


    fun customColor(@ColorInt c: Int) {
        this.currentColor = c
        this.customColor = true
    }

    fun clearCustomColor() {
        this.customColor = false
        this.currentColor = 0
    }

}