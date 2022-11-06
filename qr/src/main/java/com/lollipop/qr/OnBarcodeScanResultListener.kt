package com.lollipop.qr

fun interface OnBarcodeScanResultListener {

    fun onBarcodeScanResult(result: List<BarcodeResultWrapper>)

}