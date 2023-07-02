package com.lollipop.lqrdemo.writer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix

class QrWriterDistributor : QrWriter() {

    private var bitmap: Bitmap? = null
    private val bitmapMatrix = Matrix()

    override fun onDraw(canvas: Canvas) {
        bitmap?.let {
            canvas.drawBitmap(it, bitmapMatrix, null)
        }
    }

    override fun onBitMatrixChanged() {
        super.onBitMatrixChanged()
        bitmap = bitMatrix?.createBitmap()
        updateBitmapMatrix()
    }

    override fun onBoundsChanged() {
        super.onBoundsChanged()
        updateBitmapMatrix()
    }

    private fun updateBitmapMatrix() {
        val b = bitmap ?: return
        val scaleX = bounds.width() * 1F / b.width
        val scaleY = bounds.height() * 1F / b.height
        bitmapMatrix.setScale(scaleX, scaleY)
    }

}