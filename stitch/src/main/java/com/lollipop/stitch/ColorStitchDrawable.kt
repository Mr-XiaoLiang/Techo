package com.lollipop.stitch

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.min

class ColorStitchDrawable : Drawable() {

    private val colorList = ArrayList<Int>()
    private val pieceList = ArrayList<StitchPiece>()

    private var alphaValue = 255
    private val paint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        buildPiece()
    }

    private fun buildPiece() {
        val x = bounds.left.toFloat()
        val y = bounds.top.toFloat()
        val height = bounds.height().toFloat()
        val width = bounds.width().toFloat()
        pieceList.forEach {
            it.updatePath(x, y, width, height)
        }
        invalidateSelf()
    }

    fun resetColor(list: List<Int>, pieces: List<StitchPiece>) {
        colorList.clear()
        colorList.addAll(list)
        pieceList.clear()
        pieceList.addAll(pieces)
        buildPiece()
    }

    override fun draw(canvas: Canvas) {
        val count = min(colorList.size, pieceList.size)
        for (i in 0 until count) {
            paint.color = colorList[i]
            paint.alpha = alphaValue
            canvas.drawPath(pieceList[i].path, paint)
        }
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