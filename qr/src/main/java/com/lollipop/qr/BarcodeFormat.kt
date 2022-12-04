package com.lollipop.qr

import com.google.mlkit.vision.barcode.common.Barcode
import com.lollipop.qr.writer.ZxingWrapper

enum class BarcodeFormat(val code: Int) : ZxingWrapper<Result<com.google.zxing.BarcodeFormat>> {
    UNKNOWN(Barcode.FORMAT_UNKNOWN),
    ALL_FORMATS(Barcode.FORMAT_ALL_FORMATS),
    CODE_128(Barcode.FORMAT_CODE_128),
    CODE_39(Barcode.FORMAT_CODE_39),
    CODE_93(Barcode.FORMAT_CODE_93),
    CODABAR(Barcode.FORMAT_CODABAR),
    DATA_MATRIX(Barcode.FORMAT_DATA_MATRIX),
    EAN_13(Barcode.FORMAT_EAN_13),
    EAN_8(Barcode.FORMAT_EAN_8),
    ITF(Barcode.FORMAT_ITF),
    QR_CODE(Barcode.FORMAT_QR_CODE),
    UPC_A(Barcode.FORMAT_UPC_A),
    UPC_E(Barcode.FORMAT_UPC_E),
    PDF417(Barcode.FORMAT_PDF417),
    AZTEC(Barcode.FORMAT_AZTEC);

    override val zxing: Result<com.google.zxing.BarcodeFormat>
        get() {
            return zxingType()
        }

    private fun zxingType(): Result<com.google.zxing.BarcodeFormat> {
        return when (this) {
            UNKNOWN -> {
                Result.failure(IllegalArgumentException("UNKNOWN 不能被转换为 com.google.zxing.BarcodeFormat"))
            }
            ALL_FORMATS -> {
                Result.failure(IllegalArgumentException("ALL_FORMATS 不能被转换为 com.google.zxing.BarcodeFormat"))
            }
            CODE_128 -> {
                Result.success(com.google.zxing.BarcodeFormat.CODE_128)
            }
            CODE_39 -> {
                Result.success(com.google.zxing.BarcodeFormat.CODE_39)
            }
            CODE_93 -> {
                Result.success(com.google.zxing.BarcodeFormat.CODE_93)
            }
            CODABAR -> {
                Result.success(com.google.zxing.BarcodeFormat.CODABAR)
            }
            DATA_MATRIX -> {
                Result.success(com.google.zxing.BarcodeFormat.DATA_MATRIX)
            }
            EAN_13 -> {
                Result.success(com.google.zxing.BarcodeFormat.EAN_13)
            }
            EAN_8 -> {
                Result.success(com.google.zxing.BarcodeFormat.EAN_8)
            }
            ITF -> {
                Result.success(com.google.zxing.BarcodeFormat.ITF)
            }
            QR_CODE -> {
                Result.success(com.google.zxing.BarcodeFormat.QR_CODE)
            }
            UPC_A -> {
                Result.success(com.google.zxing.BarcodeFormat.UPC_A)
            }
            UPC_E -> {
                Result.success(com.google.zxing.BarcodeFormat.UPC_E)
            }
            PDF417 -> {
                Result.success(com.google.zxing.BarcodeFormat.PDF_417)
            }
            AZTEC -> {
                Result.success(com.google.zxing.BarcodeFormat.AZTEC)
            }
        }
    }

}