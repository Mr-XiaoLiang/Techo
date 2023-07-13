package com.lollipop.lqrdemo.writer.background

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.lollipop.lqrdemo.writer.BitmapId
import kotlin.math.max

class BitmapBackgroundWriterLayer : BackgroundWriterLayer() {

    companion object {

        var bitmap: Bitmap? = null
        var gravity = Gravity.CENTER

        fun destroyBitmap() {
            try {
                bitmap?.recycle()
                bitmap = null
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    private val matrix = Matrix()
    private var bitmapId = BitmapId()
    private var currentGravity = gravity

    override fun onDraw(canvas: Canvas) {
        bitmap?.let {
            if (checkBitmap(it)) {
                canvas.drawBitmap(it, matrix, null)
            }
        }
    }

    private fun updateMatrix(bitmap: Bitmap) {
        if (bitmap.isRecycled) {
            resetMatrix()
            return
        }
        val bWidth = bitmap.width
        val bHeight = bitmap.height
        val vWidth = bounds.width()
        val vHeight = bounds.height()
        if (bWidth < 1 || bHeight < 1 || vWidth < 1 || vHeight < 1) {
            resetMatrix()
            return
        }
        val left = bounds.left
        val top = bounds.top
        var scale = 1F
        var offsetX = 0F
        var offsetY = 0F
        currentGravity = gravity
        when (currentGravity) {
            Gravity.LEFT -> {
                scale = bHeight * 1F / vHeight
                offsetX = left
                offsetY = top
            }

            Gravity.Top -> {
                scale = bWidth * 1F / vWidth
                offsetX = left
                offsetY = top
            }

            Gravity.RIGHT -> {
                scale = bHeight * 1F / vHeight
                offsetX = vWidth - (scale * bWidth) + left
                offsetY = top
            }

            Gravity.BOTTOM -> {
                scale = bWidth * 1F / vWidth
                offsetX = left
                offsetY = vHeight - (scale * bHeight) + top
            }

            Gravity.CENTER -> {
                scale = max(bWidth * 1F / vWidth, bHeight * 1F / vHeight)
                offsetX = (vWidth - (scale * bWidth)) * 0.5F + left
                offsetY = (vHeight - (scale * bHeight)) * 0.5F + top
            }
        }
        matrix.setScale(scale, scale)
        matrix.postTranslate(offsetX, offsetY)
    }

    private fun resetMatrix() {
        matrix.setScale(1F, 1F)
        matrix.setTranslate(0F, 0F)
    }

    private fun checkBitmap(bitmap: Bitmap): Boolean {
        if (bitmapId.isChanged(bitmap, true) || currentGravity != gravity) {
            updateMatrix(bitmap)
        }
        return !bitmap.isRecycled
    }

    enum class Gravity {

        LEFT,
        Top,
        RIGHT,
        BOTTOM,
        CENTER

    }

}