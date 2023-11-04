package com.lollipop.lqrdemo.creator.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix

abstract class BitMatrixWriterLayer : QrWriterLayer() {


    protected var darkColor: Int = Color.BLACK
        private set
    protected var lightColor: Int = Color.TRANSPARENT
        private set

    protected var bitMatrix: LBitMatrix? = null
        private set

    protected val positionBounds = arrayOf(Rect(), Rect(), Rect())
    protected val timingPatternBounds = arrayOf(Rect(), Rect())
    protected val alignmentPatternBounds = ArrayList<Rect>()

    protected open val positionBoundsEnable = false
    protected open val timingPatternBoundsEnable = false
    protected open val alignmentPatternBoundsEnable = false

    protected var scaleValue = 1F

    protected fun findQrBitMatrix(callback: (LQrBitMatrix) -> Unit) {
        val matrix = bitMatrix ?: return
        if (matrix is LQrBitMatrix) {
            callback(matrix)
        }
    }

    fun setBitMatrix(matrix: LBitMatrix?) {
        this.bitMatrix = matrix
        updatePositionBounds()
        updateScale()
        onBitMatrixChanged()
    }

    override fun onBoundsChanged(bounds: Rect) {
        super.onBoundsChanged(bounds)
        updateScale()
    }

    fun setQrPointColor(dark: Int, light: Int) {
        this.darkColor = dark
        this.lightColor = light
        onBitMatrixChanged()
    }

    private fun updatePositionBounds() {
        findQrBitMatrix {
            if (positionBoundsEnable) {
                it.getLeftTopPattern(positionBounds[0])
                it.getRightTopPattern(positionBounds[1])
                it.getLeftBottomPattern(positionBounds[2])
            }
            if (timingPatternBoundsEnable) {
                it.getTimingPattern(timingPatternBounds[0], timingPatternBounds[1])
            }
            if (alignmentPatternBoundsEnable) {
                alignmentPatternBounds.clear()
                alignmentPatternBounds.addAll(it.getAlignmentPattern())
            }
        }
    }

    fun getLeftEdgeByScale(value: Float): Float {
        val half = scaleValue * 0.5F
        return value * scaleValue - half
    }

    fun getTopEdgeByScale(value: Float): Float {
        val half = scaleValue * 0.5F
        return value * scaleValue - half
    }


    fun getRightEdgeByScale(value: Float): Float {
        val half = scaleValue * 0.5F
        return value * scaleValue + half
    }


    fun getBottomEdgeByScale(value: Float): Float {
        val half = scaleValue * 0.5F
        return value * scaleValue + half
    }

    private fun updateScale() {
        scaleValue = bitMatrix?.getScale(bounds.width(), bounds.height()) ?: 1F
    }

    protected open fun onBitMatrixChanged() {}

}

interface PositionWriterLayer {
    fun drawPosition(canvas: Canvas)
}

interface AlignmentWriterLayer {
    fun drawAlignment(canvas: Canvas)
}

interface ContentWriterLayer {
    fun drawContent(canvas: Canvas)
}