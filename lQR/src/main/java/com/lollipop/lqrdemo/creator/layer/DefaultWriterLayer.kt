package com.lollipop.lqrdemo.creator.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix
import kotlin.math.max

class DefaultWriterLayer : BitMatrixWriterLayer(), AlignmentWriterLayer, ContentWriterLayer,
    PositionWriterLayer {

    override val positionBoundsEnable = true
    override val timingPatternBoundsEnable = true
    override val alignmentPatternBoundsEnable = true

    private val contentDataPath = Path()
    private val positionDataPath = Path()
    private val alignmentDataPath = Path()

    private val debugPaint by lazy {
        Paint().apply {
            color = Color.RED
        }
    }

    private val contentPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    override fun drawPosition(canvas: Canvas) {
        if (positionDataPath.isEmpty) {
            return
        }
        canvas.drawPath(positionDataPath, contentPaint)
    }

    override fun drawAlignment(canvas: Canvas) {
        if (alignmentDataPath.isEmpty) {
            return
        }
        canvas.drawPath(alignmentDataPath, contentPaint)
    }

    override fun drawContent(canvas: Canvas) {
        canvas.drawPath(contentDataPath, contentPaint)
    }

    override fun onBitMatrixChanged() {
        super.onBitMatrixChanged()
        buildContentPath()
    }

    override fun onPointColorChanged() {
        super.onPointColorChanged()
        contentPaint.setColor(darkColor)
    }

    override fun onBoundsChanged(bounds: Rect) {
        super.onBoundsChanged(bounds)
        buildContentPath()
    }

    private fun buildContentPath() {
        updateDataPointPath()
        updatePositionPointPath()
        updateAlignmentPatternPath()
    }

    private fun updateDataPointPath() {
        val path = contentDataPath
        path.reset()
        findQrBitMatrix { matrix ->
            val quietZone = matrix.quietZone
            val right = matrix.width - quietZone
            val bottom = matrix.height - quietZone
            val tempRect = Rect()
            for (x in quietZone until right) {
                var y = quietZone
                while (y < bottom) {
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

    private fun updatePositionPointPath() {
        val path = positionDataPath
        path.reset()
        positionBounds.array.forEach { rect ->
            val lineWidth = scaleValue
            addBoxToPath(path, rect, lineWidth, lineWidth * 2)
        }
    }

    private fun updateAlignmentPatternPath() {
        val path = alignmentDataPath
        path.reset()
        alignmentPatternBounds.forEach { rect ->
            val lineWidth = scaleValue
            addBoxToPath(path, rect, lineWidth, lineWidth * 2)
        }
        // 基准线暂时交给content绘制
//        val tempRect = Rect()
//        timingPatternBounds.forEach { rect ->
//            if (rect.width() != 0 || rect.height() != 0) {
//                val offsetY = if (rect.width() == 0) {
//                    2
//                } else {
//                    0
//                }
//                val offsetX = if (rect.height() == 0) {
//                    2
//                } else {
//                    0
//                }
//                val max = max(rect.right, rect.bottom)
//                tempRect.set(rect.left, rect.top, rect.left, rect.top)
//                while (tempRect.top <= max && tempRect.left <= max) {
//                    addRectToPathByScale(path, rect)
//                    tempRect.offset(offsetX, offsetY)
//                }
//            }
//        }
    }

    private fun addBoxToPath(path: Path, rect: Rect, lineWidth: Float, coreOffset: Float) {
        val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
        val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
        val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
        val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
        // 添加外框
        // 左
        path.addRect(
            leftEdgeByScale,
            topEdgeByScale,
            leftEdgeByScale + lineWidth,
            bottomEdgeByScale,
            Path.Direction.CW
        )
        // 右
        path.addRect(
            rightEdgeByScale - lineWidth,
            topEdgeByScale,
            rightEdgeByScale,
            bottomEdgeByScale,
            Path.Direction.CW
        )
        // 上
        path.addRect(
            leftEdgeByScale,
            topEdgeByScale,
            rightEdgeByScale,
            topEdgeByScale + lineWidth,
            Path.Direction.CW
        )
        // 下
        path.addRect(
            leftEdgeByScale,
            bottomEdgeByScale - lineWidth,
            rightEdgeByScale,
            bottomEdgeByScale,
            Path.Direction.CW
        )
        // 中心
        path.addRect(
            leftEdgeByScale + coreOffset,
            topEdgeByScale + coreOffset,
            rightEdgeByScale - coreOffset,
            bottomEdgeByScale - coreOffset,
            Path.Direction.CW
        )
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