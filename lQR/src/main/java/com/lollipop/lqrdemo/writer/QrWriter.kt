package com.lollipop.lqrdemo.writer

import android.graphics.Canvas
import android.graphics.Rect

abstract class QrWriter {

    protected val bounds: Rect = Rect()

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        bounds.set(left, top, right, bottom)
        onBoundsChanged()
    }

    open fun onDraw(canvas: Canvas) {}

    open fun onBoundsChanged() {}

}