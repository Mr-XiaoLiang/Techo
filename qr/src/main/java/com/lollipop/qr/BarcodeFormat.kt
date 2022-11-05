package com.lollipop.qr

import com.google.mlkit.vision.barcode.common.Barcode

enum class BarcodeFormat(val code: Int) {
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
}