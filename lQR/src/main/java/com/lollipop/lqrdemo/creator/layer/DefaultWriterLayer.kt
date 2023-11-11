package com.lollipop.lqrdemo.creator.layer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import com.lollipop.lqrdemo.BuildConfig
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix
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

    private val contentDataPath = Path()

    private val debugPaint by lazy {
        Paint().apply {
            color = Color.RED
            alpha = 0x33
        }
    }

    private val contentPaint = Paint().apply {
        style = Paint.Style.FILL
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
//        val b = bitmap ?: return
//        val count = canvas.save()
//        canvas.clipOutPath(positionClipPath)
//        canvas.clipOutPath(alignmentClipPath)
//        canvas.drawBitmap(b, bitmapMatrix, null)
//        canvas.restoreToCount(count)
        canvas.drawPath(contentDataPath, contentPaint)
    }

    override fun onBitMatrixChanged() {
        super.onBitMatrixChanged()
        bitmap = bitMatrix?.createBitmap(darkColor = darkColor, lightColor = lightColor)
        updateBitmapMatrix()
        updateClipPath()
        updateDataPointPath()
    }

    override fun onPointColorChanged() {
        super.onPointColorChanged()
        contentPaint.setColor(darkColor)
        bitmap = bitMatrix?.createBitmap(darkColor = darkColor, lightColor = lightColor)
    }

    override fun onBoundsChanged(bounds: Rect) {
        super.onBoundsChanged(bounds)
        updateBitmapMatrix()
        updateClipPath()
        updateDataPointPath()
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

    private fun updateDataPointPath() {
        val path = contentDataPath
        path.reset()
        findQrBitMatrix { matrix ->
            val quietZone = matrix.quietZone
            val width = matrix.width - quietZone
            val height = matrix.height - quietZone
            val tempRect = Rect()
            val top = quietZone
            for (x in quietZone until width) {
                var y = top
                while (y < height) {
                    if (isInAlignmentPattern(matrix, x, y)) {
                        y++
                        continue
                    }
                    val currentType = matrix.getType(x, y)
                    if (currentType == LBitMatrix.Type.BLACK) {
                        val edge = matrix.getVerticalEdge(x, y, LBitMatrix.Type.BLACK)
                        if (edge < 0) {
                            y++
                            continue
                        }
                        if (edge >= y) {
                            tempRect.set(x, y, x, edge)
                            addRectToPathByScale(path, tempRect)
                        }
                        y = edge + 1
                    } else {
                        val edge = matrix.getVerticalEdge(x, y, LBitMatrix.Type.WHITE)
                        y = if (edge < 0) {
                            y + 1
                        } else {
                            edge + 1
                        }
                    }
                }
            }
        }
    }

    private fun isInAlignmentPattern(matrix: LQrBitMatrix, x: Int, y: Int): Boolean {
        val quietZone = matrix.quietZone
        val realX = x - quietZone
        val realY = y - quietZone
        val width = matrix.width - quietZone - quietZone
        val height = matrix.height - quietZone - quietZone
        return LQrBitMatrix.inLeftTop(realX, realY)
                || LQrBitMatrix.inRightTop(width, realX, realY)
                || LQrBitMatrix.inLeftBottom(height, realX, realY)
                || LQrBitMatrix.isAlignmentPattern(matrix.version, width, realX, realY)
    }

}