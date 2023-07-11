package com.lollipop.lqrdemo.writer

import android.graphics.Canvas
import android.graphics.Rect

interface QrWriterLayer

interface BackgroundWriterLayer : QrWriterLayer {

    fun onDraw(canvas: Canvas)

    fun onBoundsChanged(bounds: Rect)

}