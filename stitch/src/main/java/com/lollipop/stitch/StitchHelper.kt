package com.lollipop.stitch

import java.util.*
import kotlin.random.Random

object StitchHelper {

    /**
     * 拆分一块矩形
     * @param count 拆分的块数
     * @param horizontalPriority 是否横向优先，当长度与宽度相等时，优先横向拆分
     * @param random 是否允许随机形状，如果设置为true，当拆分到最小粒度时，将会随机形状的梯形
     * @param viewWidth 视图的宽度，默认为单位1
     * @param viewHeight 视图的高度，默认为单位1
     * @param paddingLeft 左侧内补白，它的参考系、长度单位，需要与视图的尺寸一致，默认为0
     * @param paddingTop 上方内补白，它的参考系、长度单位，需要与视图的尺寸一致，默认为0
     * @param paddingRight 右侧内补白，它的参考系、长度单位，需要与视图的尺寸一致，默认为0
     * @param paddingBottom 下方内补白，它的参考系、长度单位，需要与视图的尺寸一致，默认为0
     */
    fun suture(
        count: Int,
        horizontalPriority: Boolean = true,
        random: Boolean = true,
        viewWidth: Int = 1,
        viewHeight: Int = 1,
        paddingLeft: Int = 0,
        paddingTop: Int = 0,
        paddingRight: Int = 0,
        paddingBottom: Int = 0
    ): List<StitchPiece> {
        if (count < 1) {
            return emptyList()
        }
        val result = ArrayList<StitchPiece>((count * 1.4F).toInt())
        val rootStitch = Stitch(
            count,
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (viewWidth - paddingLeft - paddingRight).toFloat(),
            (viewHeight - paddingTop - paddingBottom).toFloat()
        )
        val pendingList = LinkedList<Stitch>()
        pendingList.addLast(rootStitch)
        while (pendingList.isNotEmpty()) {
            val stitch = pendingList.removeFirst()
            val stitchCount = stitch.count
            if (stitchCount > 2 || (stitchCount == 2 && !random)) {
                // 如果大于2，那么说明还需要再次分割，因此需要按照矩形方正的切割
                val smaller = stitchCount / 2
                val larger = stitchCount - smaller
                rectangleBisection(
                    stitch.width,
                    stitch.height,
                    horizontalPriority,
                    smaller,
                    larger
                ) { c, x, y, width, height ->
                    val newStitch = createStitch(stitch, c, x, y, width, height)
                    pendingList.addLast(newStitch)
                }
            } else if (stitchCount == 2) {
                // 如果等于2了，那么说明可以随机划分了
                randomBisection(
                    stitch.width,
                    stitch.height,
                    horizontalPriority
                ) { x1, y1, x2, y2, x3, y3, x4, y4 ->
                    val newPiece = createPiece(stitch, x1, y1, x2, y2, x3, y3, x4, y4)
                    result.add(newPiece)
                }
            } else if (stitchCount == 1) {
                // 如果等于1，说明这是一个确定的结果了
                val newPiece = createPiece(stitch)
                result.add(newPiece)
            }
        }
        return result
    }

    private fun createStitch(
        parent: Stitch,
        count: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ): Stitch {
        return Stitch(
            count,
            moveX(parent, x),
            moveY(parent, y),
            width * parent.width,
            height * parent.height
        )
    }

    private fun createPiece(
        parent: Stitch,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        x3: Float, y3: Float,
        x4: Float, y4: Float,
    ): StitchPiece {
        return StitchPiece(
            listOf(
                StitchVertex(moveX(parent, x1), moveY(parent, y1)),
                StitchVertex(moveX(parent, x2), moveY(parent, y2)),
                StitchVertex(moveX(parent, x3), moveY(parent, y3)),
                StitchVertex(moveX(parent, x4), moveY(parent, y4)),
            )
        )
    }

    private fun createPiece(stitch: Stitch): StitchPiece {
        return StitchPiece(
            listOf(
                StitchVertex(stitch.x, stitch.y),
                StitchVertex(stitch.x + stitch.width, stitch.y),
                StitchVertex(stitch.x + stitch.width, stitch.y + stitch.height),
                StitchVertex(stitch.x, stitch.y + stitch.height)
            )
        )
    }

    private fun moveX(parent: Stitch, x: Float): Float {
        return x * parent.width + parent.x
    }

    private fun moveY(parent: Stitch, y: Float): Float {
        return y * parent.height + parent.y
    }

    /**
     * 按照矩形二分
     * @param width 宽度的权重，用于区分是纵向矩形还是横向矩形
     * @param height 高度的权重，用于区分是纵向矩形还是横向矩形
     * @param horizontalPriority 横向优先。设置为true的情况下，如果宽和高相等，那么横向优先
     * @param weightA 用于表示二分的时候第一部分的权重
     * @param weightB 用于表示二分的时候第二部分的权重
     * @param result 返回结果的回调函数
     */
    private fun rectangleBisection(
        width: Float,
        height: Float,
        horizontalPriority: Boolean,
        weightA: Int,
        weightB: Int,
        result: (weight: Int, x: Float, y: Float, width: Float, height: Float) -> Unit
    ) {
        val weight = weightA * 1F / (weightA + weightB)
        if (isHorizontal(width, height, horizontalPriority)) {
            result(weightA, 0F, 0F, weight, 1F)
            result(weightB, weight, 0F, 1 - weight, 1F)
        } else {
            result(weightA, 0F, 0F, 1F, weight)
            result(weightB, 0F, weight, 1F, 1 - weight)
        }
    }

    private fun isHorizontal(
        width: Float,
        height: Float,
        horizontalPriority: Boolean
    ): Boolean {
        val wInt = (width * 1000).toInt()
        val hInt = (height * 1000).toInt()
        return if (wInt == hInt) {
            horizontalPriority
        } else {
            wInt > hInt
        }
    }

    /**
     * 随机二分
     * @param width 宽度的权重，用于区分是纵向矩形还是横向矩形
     * @param height 高度的权重，用于区分是纵向矩形还是横向矩形
     * @param horizontalPriority 横向优先。设置为true的情况下，如果宽和高相等，那么横向优先
     * @param result 返回结果的回调函数,
     * [(x1, y1), (x2, y2), (x3, y3), (x4, y4)]
     * 分别对应[ 左上, 右上, 右下, 左下 ]
     */
    private fun randomBisection(
        width: Float,
        height: Float,
        horizontalPriority: Boolean,
        result: (
            x1: Float, y1: Float,
            x2: Float, y2: Float,
            x3: Float, y3: Float,
            x4: Float, y4: Float,
        ) -> Unit
    ) {
        if (isHorizontal(width, height, horizontalPriority)) {
            val x = Random.nextFloat()
            result(
                0F, 0F,
                x, 0F,
                1 - x, 1F,
                0F, 1F
            )
            result(
                x, 0F,
                1F, 0F,
                1F, 1F,
                1 - x, 1F
            )
        } else {
            val y = Random.nextFloat()
            result(
                0F, 0F,
                1F, 0F,
                1F, y,
                0F, 1 - y
            )
            result(
                0F, 1 - y,
                1F, y,
                1F, 1F,
                0F, 1F
            )
        }
    }

    class Stitch(
        val count: Int,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    ) {

        override fun toString(): String {
            return "Stitch(count = $count, x = $x, y = $y, width = $width, height = $height)"
        }

    }

}