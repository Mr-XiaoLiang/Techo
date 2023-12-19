package com.lollipop.lqrdemo.creator.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
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

    protected val positionBounds = PositionBounds()
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
        onPointColorChanged()
    }

    private fun updatePositionBounds() {
        findQrBitMatrix {
            if (positionBoundsEnable) {
                it.getLeftTopPattern(positionBounds.leftTopPattern)
                it.getRightTopPattern(positionBounds.rightTopPattern)
                it.getLeftBottomPattern(positionBounds.leftBottomPattern)
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

    fun addRectToPathByScale(path: Path, rect: Rect) {
        path.addRect(
            getLeftEdgeByScale(rect.left.toFloat()),
            getTopEdgeByScale(rect.top.toFloat()),
            getRightEdgeByScale(rect.right.toFloat()),
            getBottomEdgeByScale(rect.bottom.toFloat()),
            Path.Direction.CW
        )
    }

    fun getLeftEdgeByScale(value: Float): Float {
        return (value) * scaleValue - 0.5F
    }

    fun getTopEdgeByScale(value: Float): Float {
        return (value) * scaleValue - 0.5F
    }


    fun getRightEdgeByScale(value: Float): Float {
        return (value + 1F) * scaleValue + 0.5F
    }


    fun getBottomEdgeByScale(value: Float): Float {
        return (value + 1F) * scaleValue + 0.5F
    }

    private fun updateScale() {
        scaleValue = bitMatrix?.getScale(bounds.width(), bounds.height()) ?: 1F
    }

    protected open fun onBitMatrixChanged() {}

    protected open fun onPointColorChanged() {}

}

class PositionBounds {
    val leftTopPattern = Rect()
    val rightTopPattern = Rect()
    val leftBottomPattern = Rect()

    val array = arrayOf(leftTopPattern, rightTopPattern, leftBottomPattern)

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