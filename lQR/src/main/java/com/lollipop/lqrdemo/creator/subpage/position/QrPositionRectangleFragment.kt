package com.lollipop.lqrdemo.creator.subpage.position

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.PositionWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustContentFragment

class QrPositionRectangleFragment : StyleAdjustContentFragment() {

    companion object {

        private val LEFT_TOP_INFO = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val LEFT_BOTTOM_INFO = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val RIGHT_TOP_INFO = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        const val RADIUS_MAX = 1F
        const val RADIUS_MIN = 0F

    }

    class Layer : BitMatrixWriterLayer(), PositionWriterLayer {

        private val leftTopInfo = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val leftBottomInfo = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val rightTopInfo = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        override val positionBoundsEnable = true

        private val borderLeftTopPath = Path()
        private val coreLeftTopPath = Path()

        private val borderRightTopPath = Path()
        private val coreRightTopPath = Path()

        private val borderLeftBottomPath = Path()
        private val coreLeftBottomPath = Path()

        private val contentPaint = Paint()

        override fun drawPosition(canvas: Canvas) {
            checkContentPath()
            drawPosition(canvas, borderLeftTopPath, coreLeftTopPath, leftTopInfo)
            drawPosition(canvas, borderRightTopPath, coreRightTopPath, rightTopInfo)
            drawPosition(canvas, borderLeftBottomPath, coreLeftBottomPath, leftBottomInfo)
        }

        private fun drawPosition(canvas: Canvas, border: Path, core: Path, info: PositionInfo) {
            if (!border.isEmpty) {
                contentPaint.setColor(info.borderColor)
                contentPaint.style = Paint.Style.STROKE
                contentPaint.strokeWidth = scaleValue
                canvas.drawPath(border, contentPaint)
            }
            if (!core.isEmpty) {
                contentPaint.setColor(info.coreColor)
                contentPaint.style = Paint.Style.FILL
                canvas.drawPath(core, contentPaint)
            }
        }

        override fun onBitMatrixChanged() {
            super.onBitMatrixChanged()
            buildContentPath()
        }

        override fun onBoundsChanged(bounds: Rect) {
            super.onBoundsChanged(bounds)
            buildContentPath()
        }

        private fun buildContentPath(
            leftTop: Boolean = true,
            rightTop: Boolean = true,
            leftBottom: Boolean = true
        ) {
            if (leftTop) {
                leftTopInfo.copyFrom(LEFT_TOP_INFO)
                updatePositionPointPath(
                    rect = positionBounds.leftTopPattern,
                    border = borderLeftTopPath,
                    core = coreLeftTopPath,
                    coreRadius = leftTopInfo.coreRadius,
                    borderRadius = leftTopInfo.borderRadius
                )
            }
            if (rightTop) {
                rightTopInfo.copyFrom(RIGHT_TOP_INFO)
                updatePositionPointPath(
                    rect = positionBounds.rightTopPattern,
                    border = borderRightTopPath,
                    core = coreRightTopPath,
                    coreRadius = rightTopInfo.coreRadius,
                    borderRadius = rightTopInfo.borderRadius
                )
            }
            if (leftBottom) {
                leftBottomInfo.copyFrom(LEFT_BOTTOM_INFO)
                updatePositionPointPath(
                    rect = positionBounds.leftBottomPattern,
                    border = borderLeftBottomPath,
                    core = coreLeftBottomPath,
                    coreRadius = leftBottomInfo.coreRadius,
                    borderRadius = leftBottomInfo.borderRadius
                )
            }
        }

        private fun checkContentPath() {
            buildContentPath(
                leftTop = (borderLeftTopPath.isEmpty ||
                        coreLeftTopPath.isEmpty ||
                        !leftTopInfo.isSame(LEFT_TOP_INFO)),
                rightTop = (borderRightTopPath.isEmpty ||
                        coreRightTopPath.isEmpty ||
                        !rightTopInfo.isSame(RIGHT_TOP_INFO)),
                leftBottom = (borderLeftBottomPath.isEmpty ||
                        coreLeftBottomPath.isEmpty ||
                        !leftBottomInfo.isSame(LEFT_BOTTOM_INFO)),
            )
        }

        private fun updatePositionPointPath(
            rect: Rect,
            border: Path,
            core: Path,
            coreRadius: Radius,
            borderRadius: Radius
        ) {
            border.reset()
            core.reset()
            val lineWidth = scaleValue
            addBorderToPath(
                path = border,
                rect = rect,
                lineWidth = lineWidth,
                radius = borderRadius
            )
            addCoreToPath(path = core, rect = rect, coreOffset = lineWidth * 2, radius = coreRadius)
        }

        private fun addBorderToPath(path: Path, rect: Rect, lineWidth: Float, radius: Radius) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            val half = lineWidth * 0.5F
            // 添加外框
            val left = leftEdgeByScale + half
            val top = topEdgeByScale + half
            val right = rightEdgeByScale - half
            val bottom = bottomEdgeByScale - half
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                radius.pixelSize(right - left, bottom - top),
                Path.Direction.CW
            )
        }

        private fun addCoreToPath(path: Path, rect: Rect, coreOffset: Float, radius: Radius) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            val left = leftEdgeByScale + coreOffset
            val top = topEdgeByScale + coreOffset
            val right = rightEdgeByScale - coreOffset
            val bottom = bottomEdgeByScale - coreOffset
            // 中心
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                radius.pixelSize(right - left, bottom - top),
                Path.Direction.CW
            )
        }

    }

    protected class PositionInfo(
        val borderRadius: Radius,
        val coreRadius: Radius,
        @ColorInt
        var borderColor: Int,
        @ColorInt
        var coreColor: Int
    ) {

        fun copyFrom(info: PositionInfo) {
            this.borderRadius.copyFrom(info.borderRadius)
            this.coreRadius.copyFrom(info.coreRadius)
            this.borderColor = info.borderColor
            this.coreColor = info.coreColor
        }

        fun isSame(info: PositionInfo): Boolean {
            return (this.borderRadius.isSame(info.borderRadius) &&
                    this.coreRadius.isSame(info.coreRadius) &&
                    this.borderColor == info.borderColor &&
                    this.coreColor == info.coreColor
                    )
        }

    }

    protected class Radius(
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftTopX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftTopY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightTopX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightTopY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightBottomX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightBottomY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftBottomX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftBottomY: Float = 0F,
    ) {

        fun array(): FloatArray {
            return floatArrayOf(
                leftTopX,
                leftTopY,
                rightTopX,
                rightTopY,
                rightBottomX,
                rightBottomY,
                leftBottomX,
                leftBottomY,
            )
        }

        fun pixelSize(width: Float, height: Float): FloatArray {
            return floatArrayOf(
                leftTopX * width,
                leftTopY * height,
                rightTopX * width,
                rightTopY * height,
                rightBottomX * width,
                rightBottomY * height,
                leftBottomX * width,
                leftBottomY * height,
            )
        }

        fun copyFrom(info: Radius) {
            this.leftTopX = info.leftTopX
            this.leftTopY = info.leftTopY
            this.rightTopX = info.rightTopX
            this.rightTopY = info.rightTopY
            this.rightBottomX = info.rightBottomX
            this.rightBottomY = info.rightBottomY
            this.leftBottomX = info.leftBottomX
            this.leftBottomY = info.leftBottomY
        }

        fun isSame(info: Radius): Boolean {
            return (this.leftTopX == info.leftTopX &&
                    this.leftTopY == info.leftTopY &&
                    this.rightTopX == info.rightTopX &&
                    this.rightTopY == info.rightTopY &&
                    this.rightBottomX == info.rightBottomX &&
                    this.rightBottomY == info.rightBottomY &&
                    this.leftBottomX == info.leftBottomX &&
                    this.leftBottomY == info.leftBottomY
                    )
        }

    }

}