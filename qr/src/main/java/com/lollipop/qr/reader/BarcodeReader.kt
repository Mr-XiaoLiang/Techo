package com.lollipop.qr.reader

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.BarcodeType
import com.lollipop.qr.comm.BarcodeExecutor
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeResult
import com.lollipop.qr.comm.BarcodeResultBuilder
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.qr.comm.InputImageInfo

abstract class BarcodeReader(
    lifecycleOwner: LifecycleOwner
) : BarcodeExecutor(lifecycleOwner) {

    companion object {
        @JvmStatic
        fun yuv420888ToBitmap(inputImage: InputImage): Bitmap? {
            if (inputImage.format == ImageFormat.YUV_420_888.code) {
                val image = inputImage.mediaImage ?: return null
                val rotationDegrees = inputImage.rotationDegrees
                val bitmap = YuvToRgbConverter.yuv420888ToRgb(image)
                if (rotationDegrees == 0) {
                    return bitmap
                }
                // 旋转图片 动作
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                // 创建新的图片
                val resizedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.width, bitmap.height, matrix, true
                )
                if (resizedBitmap != bitmap && !bitmap.isRecycled) {
                    bitmap.recycle()
                }
                return resizedBitmap
            }
            return null
        }
    }

    var scanFormat = BarcodeFormat.entries

    protected open val resultByEmpty = true

    private val onBarcodeScanResultListener = ArrayList<OnBarcodeScanResultListener>()

    fun addOnBarcodeScanResultListener(listener: OnBarcodeScanResultListener) {
        this.onBarcodeScanResultListener.add(listener)
    }

    fun removeOnBarcodeScanResultListener(listener: OnBarcodeScanResultListener) {
        this.onBarcodeScanResultListener.remove(listener)
    }

    protected fun onDecodeSuccess(list: List<Barcode>, info: InputImageInfo, tag: String) {
        if (!resultByEmpty && list.isEmpty()) {
            return
        }
        val resultList = list.map { code ->
            BarcodeWrapper(parseBarcode(code), BarcodeResultBuilder.createCodeDescribeBy(code))
        }

        onBarcodeScanResultListener.forEach {
            it.onBarcodeScanResult(BarcodeResult(ArrayList(resultList), info, tag))
        }
    }

    private fun parseBarcode(code: Barcode): BarcodeInfo {
        return when (findBarcodeType(code.valueType)) {
            BarcodeType.UNKNOWN -> {
                BarcodeResultBuilder.createUnknown(code)
            }

            BarcodeType.CONTACT_INFO -> {
                BarcodeResultBuilder.createContactBy(code)
            }

            BarcodeType.EMAIL -> {
                BarcodeResultBuilder.createEmailBy(code)
            }

            BarcodeType.ISBN -> {
                BarcodeResultBuilder.createIsbn(code)
            }

            BarcodeType.PHONE -> {
                BarcodeResultBuilder.createPhoneBy(code)
            }

            BarcodeType.PRODUCT -> {
                BarcodeResultBuilder.createProduct(code)
            }

            BarcodeType.SMS -> {
                BarcodeResultBuilder.createSmsBy(code)
            }

            BarcodeType.TEXT -> {
                BarcodeResultBuilder.createText(code)
            }

            BarcodeType.URL -> {
                BarcodeResultBuilder.createUrlBy(code)
            }

            BarcodeType.WIFI -> {
                BarcodeResultBuilder.createWifiBy(code)
            }

            BarcodeType.GEO -> {
                BarcodeResultBuilder.createGeoBy(code)
            }

            BarcodeType.CALENDAR_EVENT -> {
                BarcodeResultBuilder.createCalendarEventBy(code)
            }

            BarcodeType.DRIVER_LICENSE -> {
                BarcodeResultBuilder.createDriverLicenseBy(code)
            }
        }
    }

    private fun findBarcodeType(code: Int): BarcodeType {
        BarcodeType.entries.forEach {
            if (it.code == code) {
                return it
            }
        }
        return BarcodeType.UNKNOWN
    }

}