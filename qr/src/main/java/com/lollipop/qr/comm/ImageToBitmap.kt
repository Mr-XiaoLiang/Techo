package com.lollipop.qr.comm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.media.Image
import android.util.Log
import androidx.core.graphics.createBitmap

object ImageToBitmap {

    fun parse(image: Image): Result<Bitmap> {
        return try {
            parseImpl(image)
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            try {
                image.close()
            } catch (e: Throwable) {
                Log.e("ImageToBitmap", "parseImpl.release error", e)
            }
        }
    }

    private fun parseImpl(image: Image): Result<Bitmap> {
        Log.d("ImageToBitmap", "format = ${image.format}, planCount = ${image.planes.size}")
        when (image.format) {
            ImageFormat.JPEG, ImageFormat.HEIC -> {
                val plane = image.planes[0]
                val buffer = plane.buffer
                val byteArray = ByteArray(buffer.capacity())
                buffer.get(byteArray)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                return Result.success(bitmap)
            }

            PixelFormat.RGBA_8888 -> {
                val plane = image.planes[0]
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride

                val width = image.width
                val height = image.height

                // 计算有效行宽（去除padding）
                val effectiveWidth = (width * pixelStride)
                val bitmapWidth = if (rowStride > effectiveWidth) {
                    rowStride / pixelStride
                } else {
                    width
                }
                Log.d(
                    "ImageToBitmap",
                    "RGBA_8888, pixelStride = $pixelStride, rowStride = $rowStride, width = $width, height = $height, bitmapWidth = $bitmapWidth"
                )
                val bitmap = createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888)
                val buffer = plane.buffer
                bitmap.copyPixelsFromBuffer(buffer)
                return Result.success(bitmap)
            }

            else -> {
                return Result.failure(Exception("不支持的图片格式"))
            }
        }
    }

}