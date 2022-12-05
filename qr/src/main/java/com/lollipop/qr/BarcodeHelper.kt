package com.lollipop.qr

import androidx.lifecycle.LifecycleOwner
import com.lollipop.qr.reader.CameraBarcodeReader
import com.lollipop.qr.reader.ImageBarcodeReader
import com.lollipop.qr.writer.BarcodeWriter
import com.lollipop.qr.writer.TypedBarcodeWriter

object BarcodeHelper {

    fun createCameraReader(lifecycleOwner: LifecycleOwner): CameraBarcodeReader {
        return CameraBarcodeReader(lifecycleOwner)
    }

    fun createLocalReader(lifecycleOwner: LifecycleOwner): ImageBarcodeReader {
        return ImageBarcodeReader(lifecycleOwner)
    }

    fun createWriter(lifecycleOwner: LifecycleOwner): TypedBarcodeWriter {
        return TypedBarcodeWriter(lifecycleOwner)
    }

}
