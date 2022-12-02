package com.lollipop.qr.reader

fun interface OnBarcodeScanResultListener {

    fun onBarcodeScanResult(result: BarcodeResult)

}