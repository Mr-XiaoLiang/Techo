package com.lollipop.lqrdemo.writer

import android.graphics.Rect
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.qr.writer.LBitMatrix

class QrWriterGroup {

    private val writerArray = ArrayList<QrWriter>()

    private val bounds: Rect = Rect()

    private var bitMatrix: LBitMatrix? = null

    private var backgroundLayer: Class<BackgroundWriterLayer>? = null

    fun addWriter(writer: QrWriter) {
        val b = bounds
        writer.setBounds(b.left, b.top, b.right, b.bottom)
        writer.setBitMatrix(bitMatrix)
        writer.setBackground(backgroundLayer)
        writerArray.add(writer)
    }

    fun setBitMatrix(matrix: LBitMatrix?) {
        this.bitMatrix = matrix
        writerArray.forEach {
            it.setBitMatrix(matrix)
        }
    }

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        bounds.set(left, top, right, bottom)
        writerArray.forEach {
            it.setBounds(left, top, right, bottom)
        }
    }

    fun setBackground(layer: Class<BackgroundWriterLayer>?) {
        backgroundLayer = layer
        writerArray.forEach {
            it.setBackground(layer)
        }
    }

    fun removeWriter(writer: QrWriter) {
        writerArray.remove(writer)
    }

    fun removeAt(index: Int) {
        if (index >= 0 && index < writerArray.size) {
            writerArray.removeAt(index)
        }
    }

}