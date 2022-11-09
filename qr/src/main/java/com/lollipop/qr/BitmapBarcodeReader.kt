package com.lollipop.qr

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BitmapBarcodeReader(lifecycleOwner: LifecycleOwner) : BarcodeReader(lifecycleOwner) {

    override val resultByEmpty: Boolean = true

    fun read(bitmap: Bitmap, tag: String) {
        TODO()
    }

    fun read(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        format: ImageFormat,
        tag: String
    ) {
//        InputImage.fromByteArray()
        TODO()
    }

//    fun read(bitmap: Bitmap, tag: String) {
//        TODO()
//    }
//
//    fun read(bitmap: Bitmap, tag: String) {
//        TODO()
//    }
//
//    fun read(bitmap: Bitmap, tag: String) {
//        TODO()
//    }
//
//    fun read(bitmap: Bitmap, tag: String) {
//        TODO()
//    }

    private fun read(protocol: ImageProtocol) {
        val inputImage = protocol.getImage()

        val array = scanFormat.map { it.code }.toIntArray()
        val first = if (array.isEmpty()) {
            Barcode.FORMAT_ALL_FORMATS
        } else {
            array[0]
        }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(first, *array)
            .build()

        BarcodeScanning.getClient(options).process(inputImage)
            .addOnSuccessListener { list ->
                onDecodeSuccess(list, protocol.tag)
            }.addOnCompleteListener {
                protocol.close()
            }.addOnCanceledListener {
                protocol.close()
            }
    }

    interface ImageProtocol {
        val tag: String

        fun getImage(): InputImage

        fun close()

    }

}