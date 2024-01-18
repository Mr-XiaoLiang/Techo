package com.lollipop.faceicon

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable

class FaceIconDrawable : Drawable() {

    private val faceBuilder = FaceBuilder()
    private val path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL
    }

    var color: Int
        get() {
            return paint.color
        }
        set(value) {
            paint.color = value
        }

    var shader: Shader?
        get() {
            return paint.shader
        }
        set(value) {
            paint.shader = value
        }

    var strokeWidth: Float
        get() {
            return paint.strokeWidth
        }
        set(value) {
            paint.strokeWidth = value
        }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updatePath()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    fun next(faceIcon: FaceIcon, progress: Float) {
        faceBuilder.next(faceIcon, progress)
        updatePath()
    }

    fun progress(progress: Float) {
        faceBuilder.progress(progress)
        updatePath()
    }

    fun rebuild() {
        faceBuilder.rebuild()
        updatePath()
    }

    private fun updatePath() {
        faceBuilder.buildPath(path, bounds)
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }
}