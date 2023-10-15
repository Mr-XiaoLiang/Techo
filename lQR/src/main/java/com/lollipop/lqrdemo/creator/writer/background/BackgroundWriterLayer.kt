package com.lollipop.lqrdemo.creator.writer.background

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.lollipop.clip.squircle.RectangleSquircle
import com.lollipop.clip.squircle.SquircleCorner
import com.lollipop.lqrdemo.creator.background.BackgroundCorner
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer
import com.lollipop.lqrdemo.creator.writer.QrWriterLayerType

abstract class BackgroundWriterLayer : QrWriterLayer() {

    override val layerType: Array<QrWriterLayerType> = arrayOf(QrWriterLayerType.BACKGROUND)

    protected val rectangleSquircle by lazy {
        RectangleSquircle()
    }

    protected val clipPath = Path()

    override fun draw(canvas: Canvas) {
        if (clipPath.isEmpty) {
            onDraw(canvas)
        } else {
            val saveCount = canvas.save()
            canvas.clipPath(clipPath)
            onDraw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }

    open fun onDraw(canvas: Canvas) {}

    override fun onBoundsChanged(bounds: Rect) {
        super.onBoundsChanged(bounds)
        buildClipPath()
    }

    override fun setCorner(c: BackgroundCorner) {
        super.setCorner(c)
        buildClipPath()
    }

    protected fun buildClipPath() {
        clipPath.reset()
        val c = backgroundCorner
        if (bounds.isEmpty || c == null || c is BackgroundCorner.None) {
            return
        }
        // 8个坐标信息，分别对应了：
        // 左上角XY，右上角XY，右下角XY，左下角XY
        // 因此，左上角是[0,1]，右上角是[2,3]，右下角是[4,5]，左下角是[6,7]
        val radius = getRadius(c)
        when (c) {
            is BackgroundCorner.Cut -> {
                // 切角模式，其实是把四边形变成了八边形（相邻点聚合可能变成四边形或者三角形，但是本质上是8个点了）
                val left = bounds.left
                val top = bounds.top
                val right = bounds.right
                val bottom = bounds.bottom
                // 左上角的贴着顶边的点
                clipPath.moveTo(left + radius[0], top)
                // 右上角贴着顶边的点
                clipPath.lineTo(right - radius[2], top)
                // 右上角贴着右边的点
                clipPath.lineTo(right, top + radius[3])
                // 右下角贴着右边的点
                clipPath.lineTo(right, bottom - radius[5])
                // 右下角贴着底边的点
                clipPath.lineTo(right - radius[4], bottom)
                // 左下角贴着底边的点
                clipPath.lineTo(left + radius[6], bottom)
                // 左下角贴着左边的点
                clipPath.lineTo(left, bottom - radius[7])
                // 左上角贴着左边的点
                clipPath.lineTo(left, top + radius[1])
                // 闭合
                clipPath.close()
            }

            BackgroundCorner.None -> {
                // 不做任何事情
                return
            }

            is BackgroundCorner.Round -> {
                clipPath.addRoundRect(bounds, radius, Path.Direction.CW)
            }

            is BackgroundCorner.Squircle -> {
                rectangleSquircle.setCorner(
                    SquircleCorner(radius[0], radius[1]),
                    SquircleCorner(radius[2], radius[3]),
                    SquircleCorner(radius[4], radius[5]),
                    SquircleCorner(radius[6], radius[7]),
                )
                rectangleSquircle.build(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt(),
                    clipPath
                )
            }
        }
    }

    protected fun getRadius(c: BackgroundCorner): FloatArray {
        val result = FloatArray(8)
        val width = bounds.width()
        val height = bounds.height()
        result[0] = getRadiusValue(c.leftTop, width)
        result[1] = getRadiusValue(c.leftTop, height)

        result[2] = getRadiusValue(c.rightTop, width)
        result[3] = getRadiusValue(c.rightTop, height)

        result[4] = getRadiusValue(c.rightBottom, width)
        result[5] = getRadiusValue(c.rightBottom, height)

        result[6] = getRadiusValue(c.leftBottom, width)
        result[7] = getRadiusValue(c.leftBottom, height)

        return result
    }

    protected fun getRadiusValue(radius: BackgroundCorner.Radius, edge: Float): Float {
        return when (radius) {
            is BackgroundCorner.Radius.Absolute -> {
                radius.value
            }

            BackgroundCorner.Radius.None -> {
                0F
            }

            is BackgroundCorner.Radius.Weight -> {
                radius.value * edge
            }
        }
    }


}