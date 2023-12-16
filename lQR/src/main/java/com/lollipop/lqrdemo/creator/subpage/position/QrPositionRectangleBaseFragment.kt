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

    // notifyContentChanged()

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

        private val contentPaint = Paint().apply {
            style = Paint.Style.FILL
        }

        override fun drawPosition(canvas: Canvas) {
            if (!borderPath.isEmpty) {
                contentPaint.setColor(borderColor)
                canvas.drawPath(borderPath, contentPaint)
            }
            if (!corePath.isEmpty) {
                contentPaint.setColor(coreColor)
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
        }

        private fun addCoreToPath(path: Path, rect: Rect, coreOffset: Float) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            // 中心
            path.addRect(
                leftEdgeByScale + coreOffset,
                topEdgeByScale + coreOffset,
                rightEdgeByScale - coreOffset,
                bottomEdgeByScale - coreOffset,
                Path.Direction.CW
            )
        }

    }

}