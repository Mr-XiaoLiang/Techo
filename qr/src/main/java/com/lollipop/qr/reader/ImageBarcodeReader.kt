package com.lollipop.qr.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lollipop.qr.comm.InputImageInfo
import java.nio.ByteBuffer

class ImageBarcodeReader(lifecycleOwner: LifecycleOwner) : BarcodeReader(lifecycleOwner) {

    override val resultByEmpty: Boolean = true

    var async = true

    fun read(bitmap: Bitmap, rotationDegrees: Int, tag: String) {
        read(BitmapImageProtocol(bitmap, rotationDegrees, tag))
    }

    fun read(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        format: ImageFormat,
        tag: String
    ) {
        read(ByteArrayImageProtocol(byteArray, width, height, rotationDegrees, format, tag))
    }

    fun read(image: Image, rotationDegrees: Int, matrix: Matrix?, tag: String) {
        read(MediaImageProtocol(image, rotationDegrees, matrix, tag))
    }

    fun read(
        byteBuffer: ByteBuffer,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        format: ImageFormat,
        tag: String
    ) {
        read(ByteBufferImageProtocol(byteBuffer, width, height, rotationDegrees, format, tag))
    }

    fun read(context: Context, uri: Uri, tag: String) {
        read(FilePathImageProtocol(context, uri, tag))
    }

    fun read(imageProxy: ImageProxy, tag: String) {
        read(ImageProxyImageProtocol(imageProxy, tag))
    }

    private fun read(protocol: ImageProtocol) {
        if (async) {
            doAsync {
                readImpl(protocol)
            }
        } else {
            readImpl(protocol)
        }
    }

    private fun readImpl(protocol: ImageProtocol) {
        val inputImage = protocol.getImage()
        if (inputImage == null) {
            try {
                protocol.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            onDecodeSuccess(emptyList(), InputImageInfo(0, 0), protocol.tag)
            return
        }

        val array = scanFormat.map { it.code }.toIntArray()
        val first = if (array.isEmpty()) {
            Barcode.FORMAT_ALL_FORMATS
        } else {
            array[0]
        }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(first, *array)
            .build()

        val info = InputImageInfo.from(inputImage)

        BarcodeScanning.getClient(options).process(inputImage)
            .addOnSuccessListener { list ->
                onDecodeSuccess(list, info, protocol.tag)
            }.addOnFailureListener {
                onDecodeSuccess(emptyList(), info, protocol.tag)
                it.printStackTrace()
                protocol.close()
            }.addOnCompleteListener {
                protocol.close()
            }.addOnCanceledListener {
                protocol.close()
            }
        return
    }

    interface ImageProtocol {
        val tag: String

        fun getImage(): InputImage?

        fun close() {}
    }

    private class BitmapImageProtocol(
        private val bitmap: Bitmap,
        private val rotationDegrees: Int,
        override val tag: String
    ) : ImageProtocol {
        override fun getImage(): InputImage? {
            try {
                if (bitmap.isRecycled) {
                    return null
                }
                return InputImage.fromBitmap(bitmap, rotationDegrees)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }
    }

    private class ByteArrayImageProtocol(
        private val byteArray: ByteArray,
        private val width: Int,
        private val height: Int,
        private val rotationDegrees: Int,
        private val format: ImageFormat,
        override val tag: String
    ) : ImageProtocol {
        override fun getImage(): InputImage? {
            try {
                return InputImage.fromByteArray(
                    byteArray,
                    width,
                    height,
                    rotationDegrees,
                    format.code
                )
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }
    }

    private class MediaImageProtocol(
        private val image: Image,
        private val rotationDegrees: Int,
        private val matrix: Matrix?,
        override val tag: String
    ) : ImageProtocol {
        override fun getImage(): InputImage? {
            try {
                return if (matrix != null) {
                    InputImage.fromMediaImage(image, rotationDegrees, matrix)
                } else {
                    InputImage.fromMediaImage(image, rotationDegrees)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }
    }

    private class ByteBufferImageProtocol(
        private val byteBuffer: ByteBuffer,
        private val width: Int,
        private val height: Int,
        private val rotationDegrees: Int,
        private val format: ImageFormat,
        override val tag: String
    ) : ImageProtocol {
        override fun getImage(): InputImage? {
            try {
                return InputImage.fromByteBuffer(
                    byteBuffer,
                    width,
                    height,
                    rotationDegrees,
                    format.code
                )
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }
    }

    private class FilePathImageProtocol(
        private val context: Context,
        private val uri: Uri,
        override val tag: String
    ) : ImageProtocol {
        override fun getImage(): InputImage? {
            try {
                return InputImage.fromFilePath(context, uri)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }
    }

    private class ImageProxyImageProtocol(
        private val imageProxy: ImageProxy,
        override val tag: String
    ) : ImageProtocol {

        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
        override fun getImage(): InputImage? {
            try {
                val image = imageProxy.image ?: return null
                return InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            } catch (e: Throwable) {
                e.printStackTrace()
                try {
                    imageProxy.close()
                } catch (e2: Throwable) {
                    e2.printStackTrace()
                }
            }
            return null
        }

        override fun close() {
            super.close()
            imageProxy.close()
        }

    }

}