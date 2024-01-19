package com.lollipop.lqrdemo.creator.subpage.datapoint

import android.graphics.Path
import android.graphics.Rect
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.qr.writer.LBitMatrix
import kotlin.math.min

class QrDataPointHorizontalFragment : QrDataPointBaseFragment() {

    override fun getWriterLayer(): Class<out BitMatrixWriterLayer> {
        return Layer::class.java
    }

    class Layer : BaseLayer() {

        override fun buildContentPath(path: Path, radius: Radius, pointSize: Float) {
            findQrBitMatrix { matrix ->
                val quietZone = matrix.quietZone
                val right = matrix.width - quietZone
                val bottom = matrix.height - quietZone
                val tempRect = Rect()
                for (y in quietZone until bottom) {
                    var x = quietZone
                    while (x < right) {
                        if (isInAlignmentPattern(matrix, x, y)) {
                            x++
                            continue
                        }
                        val currentType = matrix.getType(x, y)
                        if (currentType == LBitMatrix.Type.BLACK) {
                            val edge = matrix.getHorizontalEdge(
                                x, y, LBitMatrix.Type.BLACK
                            ) { px, py ->
                                !isInAlignmentPattern(matrix, px, py)
                            }
                            if (edge < 0) {
                                x++
                                continue
                            }
                            if (edge >= x) {
                                tempRect.set(x, y, edge, y)
                                addPoint(path, tempRect, radius, pointSize)
                            }
                            x = edge + 1
                        } else {
                            val edge = matrix.getHorizontalEdge(
                                x, y, LBitMatrix.Type.WHITE
                            ) { px, py ->
                                !isInAlignmentPattern(matrix, px, py)
                            }
                            x = if (edge < 0) {
                                x + 1
                            } else {
                                edge + 1
                            }
                        }
                    }
                }
            }
        }

        private fun addPoint(path: Path, rect: Rect, radius: Radius, pointSize: Float) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            var d = min(
                rightEdgeByScale - leftEdgeByScale,
                bottomEdgeByScale - topEdgeByScale
            )
            val offset = d * 0.5F * (1 - pointSize)
            d *= pointSize
            path.addRoundRect(
                leftEdgeByScale + offset,
                topEdgeByScale + offset,
                rightEdgeByScale - offset,
                bottomEdgeByScale - offset,
                radius.pixelSize(d, d),
                Path.Direction.CW
            )
        }

    }

}