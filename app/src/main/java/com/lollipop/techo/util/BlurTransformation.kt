package com.lollipop.techo.util

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.RenderScript
import androidx.annotation.FloatRange
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.lollipop.base.util.BlurUtil
import java.security.MessageDigest

/**
 * @author lollipop
 * @date 5/5/21 20:49
 */
class BlurTransformation private constructor(
    private val renderScript: RenderScript,
    @FloatRange(from = 1.0, to = 25.0)
    private val radius: Float = MAX_RADIUS
) : BitmapTransformation() {

    companion object {
        private const val MAX_RADIUS = 25F

        private const val VERSION = 1
        private const val ID = "com.lollipop.techo.util.BlurTransformation.$VERSION"

        fun create(
            context: Context,
            @FloatRange(from = 1.0, to = 25.0)
            radius: Float = MAX_RADIUS
        ): BlurTransformation {
            return BlurTransformation(RenderScript.create(context), radius)
        }
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val bitmap = pool.getDirty(toTransform.width, toTransform.height, Bitmap.Config.ARGB_8888)
        blurBitmap(toTransform, bitmap)
        return bitmap
//        return ThumbnailUtils.extractThumbnail(bitmap, outWidth, outHeight)
    }

    private fun blurBitmap(src: Bitmap, bitmap: Bitmap): Bitmap {
        BlurUtil.blurBitmap(renderScript, src, bitmap, radius)
        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius).toByteArray(CHARSET))
    }
}