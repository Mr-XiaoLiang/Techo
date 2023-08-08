package com.lollipop.lqrdemo.writer.background

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.lollipop.lqrdemo.writer.QrWriterLayer

abstract class BackgroundWriterLayer : QrWriterLayer() {

    protected val bounds = RectF()

    protected var backgroundCorner: Corner? = null
        private set

    protected val clipPath = Path()

    open fun draw(canvas: Canvas) {
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

    open fun onBoundsChanged(bounds: Rect) {
        this.bounds.set(bounds)
        buildClipPath()
    }

    fun setCorner(c: Corner) {
        this.backgroundCorner = c
        buildClipPath()
    }

    protected fun buildClipPath() {
        clipPath.reset()
        val c = backgroundCorner
        if (bounds.isEmpty || c == null || c is Corner.None) {
            return
        }
        // 8个坐标信息，分别对应了：
        // 左上角XY，右上角XY，右下角XY，左下角XY
        // 因此，左上角是[0,1]，右上角是[2,3]，右下角是[4,5]，左下角是[6,7]
        val radius = getRadius(c)
        when (c) {
            is Corner.Cut -> {
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

            Corner.None -> {
                // 不做任何事情
                return
            }

            is Corner.Round -> {
                clipPath.addRoundRect(bounds, radius, Path.Direction.CW)
            }
        }
    }

    protected fun getRadius(c: Corner): FloatArray {
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

    protected fun getRadiusValue(radius: Radius, edge: Float): Float {
        return when (radius) {
            is Radius.Absolute -> {
                radius.value
            }

            Radius.None -> {
                0F
            }

            is Radius.Weight -> {
                radius.value * edge
            }
        }
    }

    sealed class Corner(
        val leftTop: Radius,
        val rightTop: Radius,
        val rightBottom: Radius,
        val leftBottom: Radius
    ) {

        class Cut(
            leftTop: Radius,
            rightTop: Radius,
            rightBottom: Radius,
            leftBottom: Radius
        ) : Corner(leftTop, rightTop, rightBottom, leftBottom)

        class Round(
            leftTop: Radius,
            rightTop: Radius,
            rightBottom: Radius,
            leftBottom: Radius
        ) : Corner(leftTop, rightTop, rightBottom, leftBottom)

        object None : Corner(
            Radius.None,
            Radius.None,
            Radius.None,
            Radius.None
        )

    }

    sealed class Radius {

        class Absolute(val value: Float) : Radius()
        class Weight(val value: Float) : Radius()

        object None : Radius()

    }

}