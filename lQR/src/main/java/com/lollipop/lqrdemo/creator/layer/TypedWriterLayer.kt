package com.lollipop.lqrdemo.creator.layer

import android.graphics.Canvas
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer
import com.lollipop.qr.writer.LBitMatrix

abstract class BitMatrixWriterLayer: QrWriterLayer() {

    protected var bitMatrix: LBitMatrix? = null
        private set

    fun setBitMatrix(matrix: LBitMatrix?) {
        this.bitMatrix = matrix
        onBitMatrixChanged()
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