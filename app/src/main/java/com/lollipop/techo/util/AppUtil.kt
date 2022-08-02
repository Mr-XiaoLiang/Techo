package com.lollipop.techo.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

object AppUtil {

    /**
     * 是否做模糊处理
     */
    var isBlurHeader = true
        private set

    fun init(context: Context) {
        isBlurHeader = Preferences.ui(context).isBlurHeader
    }

    fun changeBlurHeader(context: Context, value: Boolean) {
        isBlurHeader = value
        Preferences.ui(context).isBlurHeader = value
    }

}

inline fun <reified T : ImageView> T.load(uri: Uri) {
    Glide.with(this).load(uri).into(this)
}

inline fun <reified T : ImageView> T.load(uri: String) {
    Glide.with(this).load(uri).into(this)
}