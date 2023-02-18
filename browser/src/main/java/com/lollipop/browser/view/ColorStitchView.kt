package com.lollipop.browser.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import java.util.*
import kotlin.random.Random

/**
 * 一个随机的颜色拼接效果的View
 * 它会用二分法，递归二分，直到拆分出每个块的位置
 * 它将会保证每一块颜色的面积都是一眼的
 */
class ColorStitchView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    class ColorStitchDrawable : Drawable() {

        private val colorList = ArrayList<Int>()

        private var alphaValue = 255
        private val paint = Paint().apply {
            isDither = true
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        override fun draw(canvas: Canvas) {
            TODO("Not yet implemented")
        }

        override fun setAlpha(alpha: Int) {
            alphaValue = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

    object ColorStitch {

        fun suture(count: Int): List<Piece> {
            val result = ArrayList<Piece>((count * 1.4F).toInt())
            val pendingList = LinkedList<Stitch>()
            pendingList.addLast(Stitch(count, 0F, 0F, 1F, 1F))
            while (pendingList.isNotEmpty()) {
                val stitch = pendingList.removeFirst()
                val stitchCount = stitch.count
                if (stitchCount > 2) {
                    // 如果大于2，那么说明还需要再次分割，因此需要按照矩形方正的切割
                    val smaller = stitchCount / 2
                    val larger = stitchCount - smaller
                    rectangleBisection(
                        stitch.width,
                        stitch.height,
                        smaller,
                        larger
                    ) { c, x, y, width, height ->
                        pendingList.addLast(createStitch(stitch, c, x, y, width, height))
                    }
                } else if (stitchCount == 2) {
                    // 如果等于2了，那么说明可以随机划分了
                    randomBisection(stitch.width, stitch.height) { x1, y1, x2, y2, x3, y3, x4, y4 ->
                        result.add(createPiece(stitch, x1, y1, x2, y2, x3, y3, x4, y4))
                    }
                } else if (stitchCount == 1) {
                    // 如果等于1，说明这是一个确定的结果了
                    result.add(createPiece(stitch))
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
        ): Piece {
            return Piece(
                arrayOf(
                    Vertex(moveX(parent, x1), moveY(parent, y1)),
                    Vertex(moveX(parent, x2), moveY(parent, y2)),
                    Vertex(moveX(parent, x3), moveY(parent, y3)),
                    Vertex(moveX(parent, x4), moveY(parent, y4)),
                )
            )
        }

        private fun createPiece(stitch: Stitch): Piece {
            return Piece(
                arrayOf(
                    Vertex(stitch.x, stitch.y),
                    Vertex(stitch.x + stitch.width, stitch.y),
                    Vertex(stitch.x + stitch.width, stitch.y + stitch.height),
                    Vertex(stitch.x, stitch.y + stitch.height)
                )
            )
        }

        private fun moveX(parent: Stitch, x: Float): Float {
            return x * parent.width + parent.x
        }

        private fun moveY(parent: Stitch, y: Float): Float {
            return y * parent.height * parent.y
        }

        /**
         * 按照矩形二分
         * @param width 宽度的权重，用于区分是纵向矩形还是横向矩形
         * @param height 高度的权重，用于区分是纵向矩形还是横向矩形
         * @param weightA 用于表示二分的时候第一部分的权重
         * @param weightB 用于表示二分的时候第二部分的权重
         * @param result 返回结果的回调函数
         */
        private fun rectangleBisection(
            width: Float,
            height: Float,
            weightA: Int,
            weightB: Int,
            result: (weight: Int, x: Float, y: Float, width: Float, height: Float) -> Unit
        ) {
            val weight = weightA * 1F / (weightA + weightB)
            if (width > height) {
                result(weightA, 0F, 0F, weight, 1F)
                result(weightB, weight, 0F, 1 - weight, 1F)
            } else {
                result(weightA, 0F, 0F, 1F, weight)
                result(weightB, 0F, weight, 1F, 1 - weight)
            }
        }

        /**
         * 随机二分
         * @param width 宽度的权重，用于区分是纵向矩形还是横向矩形
         * @param height 高度的权重，用于区分是纵向矩形还是横向矩形
         * @param result 返回结果的回调函数,
         * [(x1, y1), (x2, y2), (x3, y3), (x4, y4)]
         * 分别对应[ 左上, 右上, 右下, 左下 ]
         */
        private fun randomBisection(
            width: Float,
            height: Float,
            result: (
                x1: Float, y1: Float,
                x2: Float, y2: Float,
                x3: Float, y3: Float,
                x4: Float, y4: Float,
            ) -> Unit
        ) {
            if (width > height) {
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
        )

    }

    class Piece(
        val points: Array<Vertex>
    )

    class Vertex(
        val xWeight: Float,
        val yWeight: Float
    ) {

        fun x(width: Int): Int {
            return (width * xWeight).toInt()
        }

        fun y(height: Int): Int {
            return (height * yWeight).toInt()
        }

    }

}