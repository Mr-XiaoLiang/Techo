package com.lollipop.qr.writer

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.common.BitMatrix
import kotlin.math.min


/**
 * Created by lollipop on 2018/3/16.
 * @author Lollipop
 * 一个包装的矩阵，因为在二维码缩放模式下，
 * 需要标记清除允许透明的部分，黑色部分，白色部分
 */
open class LBitMatrix(val width: Int, val height: Int = width) {

    protected val nullableMatrix = BitMatrix(width, height)
    protected val blackMatrix = BitMatrix(width, height)

    companion object {
        fun copyOf(bitMatrix: BitMatrix): LBitMatrix {
            val lqrMatrix = LBitMatrix(bitMatrix.width, bitMatrix.height)
            for (width in 0 until bitMatrix.width) {
                for (height in 0 until bitMatrix.height) {
                    if (bitMatrix.get(width, height)) {
                        lqrMatrix.set(width, height)
                    }
                }
            }
            return lqrMatrix
        }

        private fun getPixelArray(
            bitMatrix: LBitMatrix,
            pixelArray: IntArray,
            darkColor: Int,
            lightColor: Int
        ): IntArray {
            val width = bitMatrix.width
            for (x in 0 until bitMatrix.width) {
                for (y in 0 until bitMatrix.height) {
                    if (bitMatrix.isNotNull(x, y)) {
                        pixelArray[y * width + x] = if (bitMatrix.isBlack(x, y)) {
                            darkColor
                        } else {
                            lightColor
                        }
                    }
                }
            }
            return pixelArray
        }
    }

    fun getScale(viewWidth: Float, viewHeight: Float): Float {
        val scaleX = viewWidth / width
        val scaleY = viewHeight / height
        return min(scaleX, scaleY)
    }

    fun isBlack(x: Int, y: Int): Boolean {
        return blackMatrix.get(x, y)
    }

    fun isNotNull(x: Int, y: Int): Boolean {
        return nullableMatrix.get(x, y)
    }

    fun setRegion(left: Int, top: Int, width: Int, height: Int, type: Type = Type.BLACK) {
        when (type) {
            Type.BLACK -> {
                nullableMatrix.setRegion(left, top, width, height)
                blackMatrix.setRegion(left, top, width, height)
            }

            Type.WHITE -> {
                nullableMatrix.setRegion(left, top, width, height)
            }

            else -> {}
        }
    }

    fun setRegionOneInNine(left: Int, top: Int, width: Int, height: Int, type: Type) {
        val widthMini = width / 3F
        val heightMini = height / 3F
        val x = (left + ((width - widthMini) / 2)).toInt().moreThan(0)
        val y = (top + ((height - heightMini) / 2)).toInt().moreThan(0)
        val w = (width - (heightMini * 2)).toInt().moreThan(1)
        val h = (height - (heightMini * 2)).toInt().moreThan(1)
        setRegion(x, y, w, h, type)
    }

    private fun Int.moreThan(min: Int): Int {
        return if (this < min) {
            min
        } else {
            this
        }
    }

    fun setNullable(x: Int, y: Int) {
        nullableMatrix.set(x, y)
    }

    fun setBlack(x: Int, y: Int) {
        blackMatrix.set(x, y)
    }

    fun set(x: Int, y: Int) {
        setNullable(x, y)
        setBlack(x, y)
    }

    fun clear() {
        nullableMatrix.clear()
        blackMatrix.clear()
    }

    fun bitMatrix(): BitMatrix {
        return blackMatrix
    }

    fun getGrid(x: Int, y: Int, grid: Grid) {
        grid.set(0, getType(x - 1, y - 1))
        grid.set(1, getType(x, y - 1))
        grid.set(2, getType(x + 1, y - 1))

        grid.set(3, getType(x - 1, y))
        grid.set(4, getType(x, y))
        grid.set(5, getType(x + 1, y))

        grid.set(6, getType(x - 1, y + 1))
        grid.set(7, getType(x, y + 1))
        grid.set(8, getType(x + 1, y + 1))
    }

    fun getVerticalEdge(
        x: Int,
        y: Int,
        type: Type,
        filter: ((x: Int, y: Int) -> Boolean) = { _, _ -> true }
    ): Int {
        if (x < 0 || y < 0) {
            return -1
        }
        if (x >= width || y >= height) {
            return -1
        }
        if (Type.NULL == type) {
            return -1
        }
        var next = -1
        for (i in y until height) {
            if (getType(x, i) != type || !filter(x, i)) {
                return next
            }
            next = i
        }
        return next
    }

    fun getHorizontalEdge(
        x: Int,
        y: Int,
        type: Type,
        filter: ((x: Int, y: Int) -> Boolean) = { _, _ -> true }
    ): Int {
        if (x < 0 || y < 0) {
            return -1
        }
        if (x >= width || y >= height) {
            return -1
        }
        if (Type.NULL == type) {
            return -1
        }
        var next = -1
        for (i in x until width) {
            if (getType(i, y) != type || !filter(i, y)) {
                return next
            }
            next = i
        }
        return next
    }

    fun getType(x: Int, y: Int): Type {
        if (x < 0 || y < 0) {
            return Type.NULL
        }
        if (x >= width || y >= height) {
            return Type.NULL
        }
        if (isBlack(x, y)) {
            return Type.BLACK
        }
        return Type.WHITE
    }

    enum class Type {
        NULL, BLACK, WHITE
    }

    class Grid {
        private val bitTypeArray = Array<Type>(9) { Type.NULL }

        fun get(index: Int): Type {
            if (index < 0 || index >= bitTypeArray.size) {
                return Type.NULL
            }
            return bitTypeArray[index]
        }

        fun set(index: Int, type: Type) {
            if (index < 0 || index >= bitTypeArray.size) {
                return
            }
            bitTypeArray[index] = type
        }

        /**
         * 正北方
         * 010
         * 000
         * 000
         */
        fun dueNorth(): Type {
            return get(1)
        }

        /**
         * 东北方
         * 001
         * 000
         * 000
         */
        fun northeast(): Type {
            return get(2)
        }

        /**
         * 正东方
         * 000
         * 001
         * 000
         */
        fun trueEast(): Type {
            return get(5)
        }

        /**
         * 东南方
         * 000
         * 000
         * 001
         */
        fun southeast(): Type {
            return get(8)
        }

        /**
         * 正南方
         * 000
         * 000
         * 010
         */
        fun dueSouth(): Type {
            return get(7)
        }

        /**
         * 西南方
         * 000
         * 000
         * 100
         */
        fun southwest(): Type {
            return get(6)
        }

        /**
         * 正西方
         * 000
         * 100
         * 000
         */
        fun dueWest(): Type {
            return get(3)
        }

        /**
         * 西北方
         * 100
         * 000
         * 000
         */
        fun northwest(): Type {
            return get(0)
        }


        /**
         * 中间
         * 000
         * 010
         * 000
         */
        fun center(): Type {
            return get(4)
        }

    }

    fun createBitmap(
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE,
        src: Bitmap? = null
    ): Bitmap {
        val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        //创建一个空的像素数组
        val pixelArray = IntArray(width * height) { Color.TRANSPARENT }
        //如果有原始图片，那么就将它复制到现有像素数组
        src?.getPixels(pixelArray, 0, width, 0, 0, width, height)
        //将二维码赋值到现有像素数组
        getPixelArray(this, pixelArray, darkColor, lightColor)
        //将像素数组赋值到图片中
        outBitmap.setPixels(pixelArray, 0, width, 0, 0, width, height)
        return outBitmap
    }

}
