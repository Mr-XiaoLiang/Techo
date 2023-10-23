package com.lollipop.lqrdemo.creator.layer

import android.graphics.Canvas

interface PositionWriterLayer {
    fun onDrawPosition(canvas: Canvas)
}

interface AlignmentWriterLayer {
    fun onDrawAlignment(canvas: Canvas)
}

interface ContentWriterLayer {
    fun onContentAlignment(canvas: Canvas)
}