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

open class QrPositionRectangleBaseFragment : StyleAdjustContentFragment() {

    companion object {

        var BORDER_RADIUS = 0F
        var CORE_RADIUS = 0F

        const val BORDER_RADIUS_MAX = 1F
        const val CORE_RADIUS_MAX = 1F

        const val BORDER_RADIUS_MIN = 0F
        const val CORE_RADIUS_MIN = 0F

        var BORDER_COLOR = Color.BLACK
        var CORE_COLOR = Color.BLACK

    }

    abstract class BaseLayer : BitMatrixWriterLayer(), PositionWriterLayer {

        @get:FloatRange(from = BORDER_RADIUS_MIN.toDouble(), to = BORDER_RADIUS_MAX.toDouble())
        open val borderRadius: Float
            get() {
                return BORDER_RADIUS
            }

        @get:FloatRange(from = CORE_RADIUS_MIN.toDouble(), to = CORE_RADIUS_MAX.toDouble())
        open val coreRadius: Float
            get() {
                return CORE_RADIUS
            }

        @get:ColorInt
        open val borderColor: Int
            get() {
                return BORDER_COLOR
            }

        @get:ColorInt
        open val coreColor: Int
            get() {
                return CORE_COLOR
            }

        override val positionBoundsEnable = true

        private val borderPath = Path()
        private val corePath = Path()

        private val contentPaint = Paint()

        override fun drawPosition(canvas: Canvas) {
            if (!borderPath.isEmpty) {
                contentPaint.setColor(borderColor)
                contentPaint.style = Paint.Style.STROKE
                contentPaint.strokeWidth = scaleValue
                canvas.drawPath(borderPath, contentPaint)
            }
            if (!corePath.isEmpty) {
                contentPaint.setColor(coreColor)
                contentPaint.style = Paint.Style.FILL
                canvas.drawPath(corePath, contentPaint)
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

        private fun buildContentPath() {
            updatePositionPointPath()
        }

        private fun updatePositionPointPath() {
            val border = borderPath
            val core = corePath
            border.reset()
            core.reset()
            positionBounds.forEach { rect ->
                val lineWidth = scaleValue
                addBorderToPath(path = border, rect = rect, lineWidth = lineWidth)
                addCoreToPath(path = core, rect = rect, coreOffset = lineWidth * 2)
            }
        }

        private fun addBorderToPath(path: Path, rect: Rect, lineWidth: Float) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            val radius = CORE_RADIUS
            val half = lineWidth * 0.5F
            // 添加外框
            path.addRoundRect(
                leftEdgeByScale + half,
                topEdgeByScale + half,
                rightEdgeByScale - half,
                bottomEdgeByScale - half,
                (rightEdgeByScale - leftEdgeByScale) * radius,
                (bottomEdgeByScale - topEdgeByScale) * radius,
                Path.Direction.CW
            )
        }

        private fun addCoreToPath(path: Path, rect: Rect, coreOffset: Float) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            val left = leftEdgeByScale + coreOffset
            val top = topEdgeByScale + coreOffset
            val right = rightEdgeByScale - coreOffset
            val bottom = bottomEdgeByScale - coreOffset
            val radius = CORE_RADIUS
            // 中心
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                (right - left) * radius,
                (bottom - top) * radius,
                Path.Direction.CW
            )
        }

    }

}