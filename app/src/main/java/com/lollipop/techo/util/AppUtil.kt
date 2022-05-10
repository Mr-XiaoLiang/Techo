package com.lollipop.techo.util

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

inline fun <reified T: ImageView> T.load(uri: Uri) {
    Glide.with(this).load(uri).into(this)
}

inline fun <reified T: ImageView> T.load(uri: String) {
    Glide.with(this).load(uri).into(this)
}