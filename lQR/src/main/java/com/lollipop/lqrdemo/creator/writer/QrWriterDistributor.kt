package com.lollipop.lqrdemo.creator.writer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import androidx.lifecycle.LifecycleOwner
import kotlin.math.min

class QrWriterDistributor(lifecycleOwner: LifecycleOwner) : QrWriter(lifecycleOwner) {

//    private var bitmap: Bitmap? = null
//    private val bitmapMatrix = Matrix()

    override fun onDraw(canvas: Canvas) {
//        bitmap?.let {
//            canvas.drawBitmap(it, bitmapMatrix, null)
//        }
    }

//    override fun onBitMatrixChanged() {
//        super.onBitMatrixChanged()
//        bitmap = bitMatrix?.createBitmap(darkColor = darkColor, lightColor = lightColor)
//        updateBitmapMatrix()
//    }
//
//    override fun onBoundsChanged() {
//        super.onBoundsChanged()
//        updateBitmapMatrix()
//    }
//
//    private fun updateBitmapMatrix() {
//        val b = bitmap ?: return
//        val scaleX = bounds.width() * 1F / b.width
//        val scaleY = bounds.height() * 1F / b.height
//        val scale = min(scaleX, scaleY)
//        bitmapMatrix.setScale(scale, scale)
//        scaleValue = scale
//        notifyBackgroundChanged()
//    }

}