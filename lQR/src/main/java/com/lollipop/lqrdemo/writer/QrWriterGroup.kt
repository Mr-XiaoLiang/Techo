package com.lollipop.lqrdemo.writer

import android.graphics.Canvas
import android.graphics.Rect
import com.google.zxing.common.BitMatrix
import com.lollipop.qr.writer.LBitMatrix

class QrWriterGroup: QrWriter() {

    private val writerArray = ArrayList<QrWriter>()

    fun addWriter(writer: QrWriter) {
        val b = bounds
        writer.setBounds(b.left, b.top, b.right, b.bottom)
        writer.setBitMatrix(bitMatrix)
        writerArray.add(writer)
    }

    fun removeWriter(writer: QrWriter) {
        writerArray.remove(writer)
    }

    fun removeAt(index: Int) {
        if (index >= 0 && index < writerArray.size) {
            writerArray.removeAt(index)
        }
    }

    override fun onBitMatrixChanged() {
        val matrix = bitMatrix
        writerArray.forEach {
            it.setBitMatrix(matrix)
        }
    }

    override fun onDraw(canvas: Canvas) {
        writerArray.forEach {
            it.onDraw(canvas)
        }
    }

    override fun onBoundsChanged() {
        val b = bounds
        writerArray.forEach {
            it.setBounds(b.left, b.top, b.right, b.bottom)
        }
    }

}