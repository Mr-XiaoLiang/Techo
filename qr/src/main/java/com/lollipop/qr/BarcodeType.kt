package com.lollipop.qr

import com.google.mlkit.vision.barcode.common.Barcode

enum class BarcodeType(val code: Int) {
    UNKNOWN(Barcode.TYPE_UNKNOWN),
    CONTACT_INFO(Barcode.TYPE_CONTACT_INFO),
    EMAIL(Barcode.TYPE_EMAIL),
    ISBN(Barcode.TYPE_ISBN),
    PHONE(Barcode.TYPE_PHONE),
    PRODUCT(Barcode.TYPE_PRODUCT),
    SMS(Barcode.TYPE_SMS),
    TEXT(Barcode.TYPE_TEXT),
    URL(Barcode.TYPE_URL),
    WIFI(Barcode.TYPE_WIFI),
    GEO(Barcode.TYPE_GEO),
    CALENDAR_EVENT(Barcode.TYPE_CALENDAR_EVENT),
    DRIVER_LICENSE(Barcode.TYPE_DRIVER_LICENSE);
}