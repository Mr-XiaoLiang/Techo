package com.lollipop.lqrdemo.writer.background

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class DefaultBackgroundWriterLayer : BackgroundWriterLayer() {

    private val color = Color.WHITE

    private val paint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val c = color
        if (clipPath.isEmpty) {
            canvas.drawColor(c)
        } else {
            paint.color = c
            canvas.drawPath(clipPath, paint)
        }
    }

}