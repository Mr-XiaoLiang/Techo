package com.lollipop.qr

import androidx.lifecycle.LifecycleOwner

object BarcodeHelper {

    fun createCameraReader(lifecycleOwner: LifecycleOwner): CameraBarcodeReader {
        return CameraBarcodeReader(lifecycleOwner)
    }

}
