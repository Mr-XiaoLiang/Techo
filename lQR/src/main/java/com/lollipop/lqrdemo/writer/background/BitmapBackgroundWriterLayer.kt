package com.lollipop.lqrdemo.writer.background

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.Gravity
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlin.math.max

class BitmapBackgroundWriterLayer : BackgroundWriterLayer() {

    private val matrix = Matrix()
    private var bitmapId = ""
    private var currentGravity = Gravity.CENTER
    private var currentBitmap: Bitmap? = null

    fun setBitmapUrl(url: String) {
        if (url != bitmapId) {
            currentBitmap = null
        }
        bitmapId = url
        loadBitmap()
    }

    private fun loadBitmap() {
        val url = bitmapId
        if (url.isEmpty()) {
            return
        }
        var targetWidth = bounds.width().toInt()
        var targetHeight = bounds.height().toInt()
        if (targetWidth < 21) {
            targetWidth = Target.SIZE_ORIGINAL
        }
        if (targetHeight < 21) {
            targetHeight = Target.SIZE_ORIGINAL
        }
        glide()?.asBitmap()?.load(url)
            ?.into(BitmapTarget(targetWidth, targetHeight, ::onBitmapLoaded))
    }

    fun setGravity(gravity: Gravity) {
        this.currentGravity = gravity
        updateMatrix()
        invalidateSelf()
    }

    override fun onBoundsChanged(bounds: Rect) {
        super.onBoundsChanged(bounds)
        updateMatrix()
        invalidateSelf()
    }

    private fun onBitmapLoaded(b: Bitmap?) {
        this.currentBitmap = b
        updateMatrix()
        invalidateSelf()
    }

    override fun onDraw(canvas: Canvas) {
        currentBitmap?.let {
            if (checkBitmap(it)) {
                canvas.drawBitmap(it, matrix, null)
            }
        }
    }

    private fun updateMatrix() {
        val bitmap = this.currentBitmap ?: return
        if (bitmap.isRecycled) {
            resetMatrix()
            return
        }
        val bWidth = bitmap.width
        val bHeight = bitmap.height
        val vWidth = bounds.width()
        val vHeight = bounds.height()
        if (bWidth < 1 || bHeight < 1 || vWidth < 1 || vHeight < 1) {
            resetMatrix()
            return
        }
        val left = bounds.left
        val top = bounds.top
        var scale = 1F
        var offsetX = 0F
        var offsetY = 0F
        when (currentGravity) {
            Gravity.LEFT -> {
                scale = bHeight * 1F / vHeight
                offsetX = left
                offsetY = top
            }

            Gravity.Top -> {
                scale = bWidth * 1F / vWidth
                offsetX = left
                offsetY = top
            }

            Gravity.RIGHT -> {
                scale = bHeight * 1F / vHeight
                offsetX = vWidth - (scale * bWidth) + left
                offsetY = top
            }

            Gravity.BOTTOM -> {
                scale = bWidth * 1F / vWidth
                offsetX = left
                offsetY = vHeight - (scale * bHeight) + top
            }

            Gravity.CENTER -> {
                scale = max(bWidth * 1F / vWidth, bHeight * 1F / vHeight)
                offsetX = (vWidth - (scale * bWidth)) * 0.5F + left
                offsetY = (vHeight - (scale * bHeight)) * 0.5F + top
            }
        }
        matrix.setScale(scale, scale)
        matrix.postTranslate(offsetX, offsetY)
    }

    private fun resetMatrix() {
        matrix.setScale(1F, 1F)
        matrix.setTranslate(0F, 0F)
    }

    private fun checkBitmap(bitmap: Bitmap): Boolean {
        return !bitmap.isRecycled
    }

    enum class Gravity {

        LEFT,
        Top,
        RIGHT,
        BOTTOM,
        CENTER

    }

    private class BitmapTarget(
        width: Int,
        height: Int,
        private val callback: (Bitmap?) -> Unit
    ) : CustomTarget<Bitmap>(width, height) {

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            callback(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            callback(null)
        }

    }

}