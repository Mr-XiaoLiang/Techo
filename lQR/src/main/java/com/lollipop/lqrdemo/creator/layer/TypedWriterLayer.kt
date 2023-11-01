package com.lollipop.lqrdemo.creator.layer

import android.graphics.Canvas
import android.graphics.Color
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

    protected fun findQrBitMatrix(callback: (LQrBitMatrix) -> Unit) {
        val matrix = bitMatrix ?: return
        if (matrix is LQrBitMatrix) {
            callback(matrix)
        }
    }

    fun setBitMatrix(matrix: LBitMatrix?) {
        this.bitMatrix = matrix
        onBitMatrixChanged()
    }

    fun setQrPointColor(dark: Int, light: Int) {
        this.darkColor = dark
        this.lightColor = light
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