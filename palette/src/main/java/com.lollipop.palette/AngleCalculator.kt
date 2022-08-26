package com.lollipop.palette

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal object AngleCalculator {

    /**
     * 获取两点之间的距离
     */
    fun getLength(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 通过圆心角计算位置信息
     * 数学坐标系中的[0~360]为逆时针旋转，绘图坐标系中是顺时针
     * 所以这里以顺时针来计算
     */
    fun getCoordinate(cX: Float, cY: Float, radius: Float, angle: Float): FloatArray {
        val radians = Math.toRadians(angle.toDouble())
        val x = radius * cos(radians) + cX
        val y = radius * sin(radians) + cY
        return floatArrayOf(x.toFloat(), y.toFloat())
    }

    /**
     * 获取一段弧形的长度
     */
    fun getArcLength(radius: Float, angle: Float): Float {
        return (radius * Math.PI * 2 * angle / 360).toFloat()
    }

    /**
     * 计算圆周角，
     * 圆周角的计算依据是{结束点}以{原点}为中心，
     * 以{原点所在水平线的正半轴}的线为起点，顺时针旋转的角度
     * 叠加的条件是结束点的Y值小于原点的Y值，因此要求起始点的Y等于原点的Y
     * @param oX 原点的X
     * @param oY 原点的Y
     * @param eX 结束点的X
     * @param eY 结束点的Y
     * @return 角度的值，范围[0, 360]
     */
    fun getCircumferential(
        oX: Float, oY: Float,
        eX: Float, eY: Float
    ): Float {
        val angle = calculationAngle(oX, oY, oX + 100, oY, eX, eY)
        return if (eY < oY) {
            360 - angle
        } else {
            angle
        }
    }

    /**
     * 根据余弦定理计算线段1到线段2的夹角，线段1：起始点到原点，线段2：原点到结束点）
     * @param oX 原点的X
     * @param oY 原点的Y
     * @param sX 起始点的X
     * @param sY 起始点的Y
     * @param eX 结束点的X
     * @param eY 结束点的Y
     * @return 角度的值，范围[0, 180]
     */
    private fun calculationAngle(
        oX: Float,
        oY: Float,
        sX: Float,
        sY: Float,
        eX: Float,
        eY: Float
    ): Float {
        val dsx = sX - oX
        val dsy = sY - oY
        val dex = eX - oX
        val dey = eY - oY
        var cosFi = (dsx * dex + dsy * dey).toDouble()
        val norm = ((dsx * dsx + dsy * dsy) * (dex * dex + dey * dey)).toDouble()
        cosFi /= sqrt(norm)
        if (cosFi >= 1.0) return 0F
        if (cosFi <= -1.0) return Math.PI.toFloat()
        val fi = acos(cosFi)
        return if (180 * fi / Math.PI < 180) {
            180 * fi / Math.PI
        } else {
            360 - 180 * fi / Math.PI
        }.toFloat()
    }
}