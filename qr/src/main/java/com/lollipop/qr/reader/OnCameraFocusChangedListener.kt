package com.lollipop.qr.reader

fun interface OnCameraFocusChangedListener {

    fun onCameraFocusChanged(isSuccessful: Boolean, x: Float, y: Float)

}