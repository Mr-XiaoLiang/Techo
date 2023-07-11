package com.lollipop.lqrdemo.writer.background

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import com.lollipop.lqrdemo.writer.BackgroundWriterLayer

class ColorBackgroundWriterLayer: BackgroundWriterLayer {

    companion object {

        var color = Color.WHITE

    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(color)
    }

    override fun onBoundsChanged(bounds: Rect) {
        // 不用做任何事情，只要绘制就好了
    }
}