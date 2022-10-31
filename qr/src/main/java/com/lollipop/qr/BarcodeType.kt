package com.lollipop.qr

import com.google.mlkit.vision.barcode.common.Barcode

enum class BarcodeType(val code: Int) {
    TYPE_UNKNOWN(Barcode.TYPE_UNKNOWN),
    TYPE_CONTACT_INFO(Barcode.TYPE_CONTACT_INFO),
    TYPE_EMAIL(Barcode.TYPE_EMAIL),
    TYPE_ISBN(Barcode.TYPE_ISBN),
    TYPE_PHONE(Barcode.TYPE_PHONE),
    TYPE_PRODUCT(Barcode.TYPE_PRODUCT),
    TYPE_SMS(Barcode.TYPE_SMS),
    TYPE_TEXT(Barcode.TYPE_TEXT),
    TYPE_URL(Barcode.TYPE_URL),
    TYPE_WIFI(Barcode.TYPE_WIFI),
    TYPE_GEO(Barcode.TYPE_GEO),
    TYPE_CALENDAR_EVENT(Barcode.TYPE_CALENDAR_EVENT),
    TYPE_DRIVER_LICENSE(Barcode.TYPE_DRIVER_LICENSE);
}