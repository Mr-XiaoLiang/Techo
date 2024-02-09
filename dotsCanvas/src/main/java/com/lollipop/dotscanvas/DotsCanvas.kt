package com.lollipop.dotscanvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object DotsCanvas {

    /**
     * 从文件读取，并且输出到文件
     * @param srcFile 输入文件
     * @param rowLength 每行数量
     * @param outWidth 输出文件宽度，像素数量
     * @param grayStyle 是否灰阶模式
     * @param outDir 输出文件夹
     * @return 生成的文件
     */
    suspend fun CoroutineScope.create(
        srcFile: File,
        rowLength: Int,
        outWidth: Int,
        grayStyle: Boolean = false,
        outDir: File
    ): Result<File> {
        return withContext(Dispatchers.IO) {
            createSync(srcFile, rowLength, outWidth, grayStyle, outDir)
        }
    }

    /**
     * 从文件读取，并且输出到文件
     * @param srcFile 输入文件
     * @param rowLength 每行数量
     * @param outWidth 输出文件宽度，像素数量
     * @param grayStyle 是否灰阶模式
     * @param outDir 输出文件夹
     * @return 生成的文件
     */
    private fun createSync(
        srcFile: File,
        rowLength: Int,
        outWidth: Int,
        grayStyle: Boolean = false,
        outDir: File
    ): Result<File> {
        try {
            val srcResult = DotsCanvasBitmapHelper.readSrcBitmap(srcFile, rowLength)
            val srcBitmap = srcResult.getOrNull() ?: return Result.failure(
                srcResult.exceptionOrNull()
                    ?: IllegalArgumentException("src file is not a picture")
            )
            val outResult = draw(srcBitmap, rowLength, outWidth, grayStyle)
            val outBitmap = outResult.getOrNull() ?: return Result.failure(
                outResult.exceptionOrNull()
                    ?: IllegalArgumentException("out picture error")
            )
            val fileName = System.currentTimeMillis().toString(16).uppercase()
            val outFile = File(outDir, "$fileName.png")
            if (!outDir.exists()) {
                outDir.mkdirs()
            }
            val outputStream = FileOutputStream(outFile)
            outBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return Result.success(outFile)
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun CoroutineScope.drawAsync(
        src: Bitmap,
        rowLength: Int,
        outWidth: Int,
        grayStyle: Boolean = false
    ): Result<Bitmap> {
        return withContext(Dispatchers.IO) {
            draw(src, rowLength, outWidth, grayStyle)
        }
    }

    suspend fun CoroutineScope.drawAsync(
        src: Bitmap,
        rowLength: Int,
        out: Bitmap,
        grayStyle: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            draw(src, rowLength, out, grayStyle)
        }
    }

    fun draw(
        src: Bitmap,
        rowLength: Int,
        outWidth: Int,
        grayStyle: Boolean = false
    ): Result<Bitmap> {
        try {
            if (rowLength < 1) {
                return Result.failure(IllegalArgumentException("rowLength < 1"))
            }
            if (src.width < 1 || src.height < 1 || outWidth < 1) {
                return Result.failure(IllegalArgumentException("outWidth < 1"))
            }
            val srcWidth = src.width
            val srcHeight = src.height
            val ratio = srcWidth * 1F / srcHeight
            var outHeight = (outWidth / ratio).toInt()
            val diameter = outWidth / rowLength
            val overflow = outHeight % diameter
            if (overflow != 0) {
                outHeight += diameter - overflow
            }
            val outBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
            draw(src, rowLength, outBitmap, grayStyle)
            return Result.success(outBitmap)
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

    fun draw(src: Bitmap, rowLength: Int, out: Bitmap, grayStyle: Boolean = false) {
        if (rowLength < 1) {
            return
        }
        if (src.width < 1 || src.height < 1 || out.width < 1 || out.height < 1) {
            return
        }
        val radius = out.width / rowLength / 2
        if (radius < 1) {
            return
        }
        if (src.width == rowLength) {
            // 可以直接使用，不做重新采样
            drawDots(src, radius, out, grayStyle)
        } else {
            // 需要缩放重新采样
            val newBitmap = scaleBitmap(src, rowLength)
            drawDots(newBitmap, radius, out, grayStyle)
        }
    }

    private fun drawDots(
        input: Bitmap,
        radius: Int,
        out: Bitmap,
        grayStyle: Boolean,
        background: Int = Color.BLACK
    ) {
        val diameter = radius * 2
        var lineCount = out.height / diameter
        if ((out.height % diameter) != 0) {
            lineCount += 1
        }
        val width = input.width
        val height = min(lineCount, input.height)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL
        val canvas = Canvas(out)
        canvas.drawColor(background)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = input.getPixel(x, y)
                paint.color = getColor(pixel, grayStyle)
                val left = x * diameter * 1F
                val top = y * diameter * 1F
                canvas.drawOval(left, top, left + diameter, top + diameter, paint)
            }
        }
    }

    private fun getColor(pixel: Int, grayStyle: Boolean): Int {
        if (grayStyle) {
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            var y = ((0.299F * r) + (0.587F * g) + (0.114F * b)).toInt()
            if (y > 255) {
                y = 255
            }
            if (y < 0) {
                y = 0
            }
            return Color.argb(Color.alpha(pixel), y, y, y)
        }
        return pixel
    }

    private fun scaleBitmap(origin: Bitmap, newWidth: Int): Bitmap {
        // 原始尺寸
        val originWidth = origin.getWidth()
        val originHeight = origin.getHeight()
        // 缩放比例
        val ratio = newWidth * 1F / originWidth
        // 计算出等比高度
        val newHeight = (originHeight * ratio).toInt()
        // 矩阵缩放
        val matrix = Matrix()
        matrix.preScale(ratio, ratio)
        // 创建新的Bitmap
        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(origin, matrix, null)
        return newBitmap
    }

}