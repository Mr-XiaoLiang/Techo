package com.lollipop.dotscanvas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object DotsCanvasBitmapHelper {

    /**
     * 通过二次采样读取接近缩略图采样尺寸的图片
     * 可以避免高分辨率图片在采样时发生的OOM问题
     */
    fun readSrcBitmap(file: File, lineLength: Int): Result<Bitmap> {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val bitmapBounds = BitmapFactory.decodeFile(file.path, options)
            val srcWidth = bitmapBounds.width
            var inSampleSize = 1
            var currentWidth = srcWidth
            // 我们允许输出结果的范围是：max = 1.5倍，
            // 因为每多一次循环，都会导致结果减半，那么在部分场景下，可能导致图片变得过小
            // 最极端的情况下，是接近1.5倍，但是超过了1.5倍，那么得到的结果是原本的0.75倍，仍然是接近1倍的
            while ((currentWidth * 1F / lineLength) > 1.5F) {
                inSampleSize *= 2
                currentWidth = srcWidth / inSampleSize
            }
            options.inSampleSize = inSampleSize
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            return Result.success(BitmapFactory.decodeFile(file.path, options))
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

}