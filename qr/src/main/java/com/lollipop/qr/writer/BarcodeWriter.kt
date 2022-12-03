package com.lollipop.qr.writer

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import com.lollipop.qr.comm.BarcodeExecutor

class BarcodeWriter(
    lifecycleOwner: LifecycleOwner
) : BarcodeExecutor(lifecycleOwner) {

    companion object {
        /** 三个方位角的定位点尺寸 **/
        const val POSITION_DETECTION_PATTERN_SIZE = 7

        /** 辅助定位点尺寸 **/
        const val ALIGNMENT_PATTERN_SIZE = 5

        /** Version Information 的高度 **/
        const val VERSION_INFORMATION_HEIGHT = 3

        /** Version Information 的宽度 **/
        const val VERSION_INFORMATION_WIDTH = 6

    }

    private fun drawBitmap(
        bitMatrix: LBitMatrix,
        darkColor: Int,
        lightColor: Int,
        outBitmap: Bitmap,
        src: Bitmap? = null
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        //创建一个空的像素数组
        val pixelArray = IntArray(width * height) { Color.WHITE }
        //如果有原始图片，那么就将它复制到现有像素数组
        src?.getPixels(pixelArray, 0, width, 0, 0, width, height)
        //将二维码赋值到现有像素数组
        getPixelArray(bitMatrix, pixelArray, darkColor, lightColor)
        //将像素数组赋值到图片中
        outBitmap.setPixels(pixelArray, 0, width, 0, 0, width, height)
        return outBitmap
    }

//    fun getPixels(): IntArray{
//        return getPixelArray(encode())
//    }

    private fun getPixelArray(
        bitMatrix: LBitMatrix,
        darkColor: Int,
        lightColor: Int
    ): IntArray {
        return getPixelArray(
            bitMatrix,
            IntArray(bitMatrix.width * bitMatrix.width) { Color.WHITE },
            darkColor,
            lightColor
        )
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