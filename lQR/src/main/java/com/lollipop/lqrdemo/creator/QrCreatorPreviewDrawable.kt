package com.lollipop.lqrdemo.creator

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.RequestManager
import com.lollipop.base.graphics.LDrawable
import com.lollipop.lqrdemo.creator.writer.QrWriter

class QrCreatorPreviewDrawable(private val writer: QrWriter): LDrawable() {

    override fun draw(canvas: Canvas) {
        writer.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        writer.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

}