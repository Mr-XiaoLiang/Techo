package com.lollipop.qr.reader

import com.lollipop.qr.comm.BarcodeResult

fun interface OnBarcodeScanResultListener {

    fun onBarcodeScanResult(result: BarcodeResult)

}