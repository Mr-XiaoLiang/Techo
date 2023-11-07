package com.lollipop.lqrdemo.creator.layer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import com.lollipop.lqrdemo.BuildConfig
import kotlin.math.min

class DefaultWriterLayer : BitMatrixWriterLayer(), AlignmentWriterLayer, ContentWriterLayer,
    PositionWriterLayer {

    private var bitmap: Bitmap? = null
    private val bitmapMatrix = Matrix()

    override val positionBoundsEnable = true
    override val timingPatternBoundsEnable = true
    override val alignmentPatternBoundsEnable = true

    private val positionClipPath = Path()
    private val alignmentClipPath = Path()
    private val contentClipPath = Path()

    private val debugPaint by lazy {
        Paint().apply {
            color = Color.RED
            alpha = 0x33
        }
    }

    override fun drawPosition(canvas: Canvas) {
        if (positionClipPath.isEmpty) {
            return
        }
        val b = bitmap ?: return
        val count = canvas.save()
        canvas.clipPath(positionClipPath)
        canvas.drawBitmap(b, bitmapMatrix, null)
        canvas.restoreToCount(count)
        if (BuildConfig.DEBUG) {
            canvas.drawPath(positionClipPath, debugPaint)
        }
    }

    override fun drawAlignment(canvas: Canvas) {
        if (alignmentClipPath.isEmpty) {
            return
        }
        val b = bitmap ?: return
        val count = canvas.save()
        canvas.clipPath(alignmentClipPath)
        canvas.drawBitmap(b, bitmapMatrix, null)
        canvas.restoreToCount(count)
        if (BuildConfig.DEBUG) {
            canvas.drawPath(alignmentClipPath, debugPaint)
        }
    }

    override fun drawContent(canvas: Canvas) {
        val b = bitmap ?: return
        val count = canvas.save()
        canvas.clipOutPath(positionClipPath)
        canvas.clipOutPath(alignmentClipPath)
        canvas.drawBitmap(b, bitmapMatrix, null)
        canvas.restoreToCount(count)
    }

    override fun onBitMatrixChanged() {
        super.onBitMatrixChanged()
        bitmap = bitMatrix?.createBitmap(darkColor = darkColor, lightColor = lightColor)
        updateBitmapMatrix()
        updateClipPath()
    }

    override fun onBoundsChanged(bounds: Rect) {
        super.onBoundsChanged(bounds)
        updateBitmapMatrix()
        updateClipPath()
    }

    private fun updateBitmapMatrix() {
        val b = bitmap ?: return
        val scaleX = bounds.width() * 1F / b.width
        val scaleY = bounds.height() * 1F / b.height
        val scale = min(scaleX, scaleY)
        bitmapMatrix.setScale(scale, scale)
    }

    private fun updateClipPath() {
        positionClipPath.reset()
        alignmentClipPath.reset()
        contentClipPath.reset()

        positionBounds.forEach { rect ->
            addRectToPathByScale(positionClipPath, rect)
        }

        alignmentPatternBounds.forEach { rect ->
            addRectToPathByScale(alignmentClipPath, rect)
        }
        timingPatternBounds.forEach { rect ->
            addRectToPathByScale(alignmentClipPath, rect)
        }
    }

}