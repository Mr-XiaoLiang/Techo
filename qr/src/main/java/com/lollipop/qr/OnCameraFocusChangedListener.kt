package com.lollipop.qr

fun interface OnCameraFocusChangedListener {

    fun onCameraFocusChanged(isSuccessful: Boolean, x: Float, y: Float)

}