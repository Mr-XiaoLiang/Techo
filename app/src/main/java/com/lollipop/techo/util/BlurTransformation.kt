package com.lollipop.techo.util

import android.graphics.Bitmap
import androidx.annotation.IntRange
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.google.android.renderscript.Toolkit
import java.security.MessageDigest

/**
 * @author lollipop
 * @date 5/5/21 20:49
 */
class BlurTransformation private constructor(
    @IntRange(from = 1, to = 25)
    private val radius: Int = MAX_RADIUS
) : BitmapTransformation() {

    companion object {
        private const val MAX_RADIUS = 25

        private const val VERSION = 1
        private const val ID = "com.lollipop.techo.util.BlurTransformation.$VERSION"

        fun create(
            @IntRange(from = 1, to = 25)
            radius: Int = MAX_RADIUS
        ): BlurTransformation {
            return BlurTransformation(radius)
        }
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return blurBitmap(toTransform)
    }

    private fun blurBitmap(src: Bitmap): Bitmap {
        return Toolkit.blur(src, radius)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius).toByteArray(CHARSET))
    }
}