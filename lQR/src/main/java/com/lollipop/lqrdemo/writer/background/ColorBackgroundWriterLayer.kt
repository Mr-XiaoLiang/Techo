package com.lollipop.lqrdemo.writer.background

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.annotation.ColorInt

class ColorBackgroundWriterLayer : BackgroundWriterLayer() {

    companion object {

        var color = Color.WHITE

    }

    private var currentColor = 0
    private var customColor = false

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(
            if (customColor) {
                currentColor
            } else {
                color
            }
        )
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