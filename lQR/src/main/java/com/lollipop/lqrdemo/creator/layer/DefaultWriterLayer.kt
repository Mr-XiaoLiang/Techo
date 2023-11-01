package com.lollipop.lqrdemo.creator.layer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import kotlin.math.min

class DefaultWriterLayer : BitMatrixWriterLayer(), AlignmentWriterLayer, ContentWriterLayer,
    PositionWriterLayer {

    private var bitmap: Bitmap? = null
    private val bitmapMatrix = Matrix()
    private var scaleValue = 1F

    private val positionRect = arrayOf(Rect(), Rect(), Rect())
    private val timingPatternRect = arrayOf(Rect(), Rect())
    private val alignmentPattern = ArrayList<Rect>()

    override fun drawPosition(canvas: Canvas) {
        TODO("Not yet implemented")
    }

    override fun drawAlignment(canvas: Canvas) {
        TODO("Not yet implemented")
    }

    override fun drawContent(canvas: Canvas) {
        TODO("Not yet implemented")
    }

    override fun onBitMatrixChanged() {
        super.onBitMatrixChanged()
        bitmap = bitMatrix?.createBitmap(darkColor = darkColor, lightColor = lightColor)
        findQrBitMatrix {
            it.getLeftTopPattern(positionRect[0])
            it.getRightTopPattern(positionRect[1])
            it.getLeftBottomPattern(positionRect[2])
            it.getTimingPattern(timingPatternRect[0], timingPatternRect[1])
            alignmentPattern.clear()
            alignmentPattern.addAll(it.getAlignmentPattern())
        }
        updateBitmapMatrix()
    }

    override fun onBoundsChanged(bounds: Rect) {
        super.onBoundsChanged(bounds)
        updateBitmapMatrix()
    }

    private fun updateBitmapMatrix() {
        val b = bitmap ?: return
        val scaleX = bounds.width() * 1F / b.width
        val scaleY = bounds.height() * 1F / b.height
        val scale = min(scaleX, scaleY)
        bitmapMatrix.setScale(scale, scale)
        scaleValue = scale
    }

}