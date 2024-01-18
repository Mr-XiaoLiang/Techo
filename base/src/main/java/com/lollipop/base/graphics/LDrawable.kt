package com.lollipop.base.graphics

import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

abstract class LDrawable: Drawable() {

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

}