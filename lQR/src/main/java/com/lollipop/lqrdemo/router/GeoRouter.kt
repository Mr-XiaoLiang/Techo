package com.lollipop.lqrdemo.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lollipop.qr.comm.BarcodeInfo

object GeoRouter : BarcodeRouter<BarcodeInfo.GeoPoint>() {

    override fun getIntent(context: Context, barcodeInfo: BarcodeInfo.GeoPoint): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:${barcodeInfo.lat},${barcodeInfo.lng}")
        )
    }

}