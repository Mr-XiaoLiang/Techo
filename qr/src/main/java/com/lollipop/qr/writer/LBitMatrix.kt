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

    fun setRegion(left: Int, top: Int, width: Int, height: Int, type: TYPE = TYPE.BLACK) {
        when (type) {
            TYPE.BLACK -> {
                nullableMatrix.setRegion(left, top, width, height)
                blackMatrix.setRegion(left, top, width, height)
            }

            TYPE.WHITE -> {
                nullableMatrix.setRegion(left, top, width, height)
            }

            else -> {}
        }
    }

    fun setRegionOneInNine(left: Int, top: Int, width: Int, height: Int, type: TYPE) {
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

    enum class TYPE {
        NULL, BLACK, WHITE
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
