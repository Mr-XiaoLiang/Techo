package com.lollipop.lqrdemo.writer

import android.graphics.Canvas
import android.graphics.Rect
import com.google.zxing.common.BitMatrix
import com.lollipop.qr.writer.LBitMatrix

abstract class QrWriter {

    protected val bounds: Rect = Rect()

    protected var bitMatrix: LBitMatrix? = null
        private set

    fun setBitMatrix(matrix: LBitMatrix?) {
        this.bitMatrix = matrix
        onBitMatrixChanged()
    }

    protected open fun onBitMatrixChanged() {}

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        bounds.set(left, top, right, bottom)
        onBoundsChanged()
    }

    open fun onDraw(canvas: Canvas) {}

    open fun onBoundsChanged() {}

}