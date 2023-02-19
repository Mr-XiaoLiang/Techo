package com.lollipop.stitch

import android.graphics.Path
import android.graphics.RectF

class StitchPiece(
    private val points: List<StitchVertex>
) : List<StitchVertex> by points {

    /**
     * 获取当前碎片的外切矩形
     */
    val bounds: Bounds by lazy {
        findRectF()
    }

    val path: Path by lazy {
        Path()
    }

    fun updatePath(x: Float = 0F, y: Float = 0F, width: Float = 1F, height: Float = 1F) {
        path.reset()
        if (points.size < 2) {
            return
        }
        for (index in points.indices) {
            val point = points[index]
            if (index == 0) {
                path.moveTo(point.x(width) + x, point.y(height) + y)
            } else {
                path.lineTo(point.x(width) + x, point.y(height) + y)
            }
        }
        path.close()
    }

    private fun findRectF(): Bounds {
        var left = 0F
        var top = 0F
        var right = 0F
        var bottom = 0F
        points.forEach {
            if (it.xWeight < left) {
                left = it.xWeight
            }
            if (it.xWeight > right) {
                right = it.xWeight
            }
            if (it.yWeight < top) {
                top = it.yWeight
            }
            if (it.yWeight > bottom) {
                bottom = it.yWeight
            }
        }
        return Bounds(left, top, right, bottom)
    }

    class Bounds(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    ) {

        fun copyTo(rectF: RectF) {
            rectF.set(left, top, right, bottom)
        }

    }

}