package com.lollipop.qr.comm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.media.Image

object ImageToBitmap {

    fun parse(image: Image): Result<Bitmap> {
        return try {
            parseImpl(image)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun parseImpl(image: Image): Result<Bitmap> {
        when (image.format) {
            ImageFormat.JPEG, ImageFormat.HEIC -> {
                return Result.success(compressedData(image))
            }

            else -> {
                return Result.failure(Exception("不支持的图片格式"))
            }
        }
    }

    // val plane = image.planes[0]
    //                val width = image.width
    //                val height = image.height
    //                // 重新计算Bitmap宽度，防止Bitmap显示错位
    //                val pixelStride = plane.pixelStride;
    //                val rowStride = plane.rowStride;
    //                val rowPadding = rowStride - pixelStride * width;
    //                val bitmapWidth = width + rowPadding / pixelStride;
    //
    //                // 创建Bitmap
    //                val bitmap = createBitmap(bitmapWidth, height);
    //                val byteBuffer = plane.buffer
    //                bitmap.copyPixelsFromBuffer(byteBuffer);
    //                return Result.success(bitmap)

    private fun compressedData(image: Image): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val byteArray = ByteArray(buffer.capacity())
        buffer.get(byteArray)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

}