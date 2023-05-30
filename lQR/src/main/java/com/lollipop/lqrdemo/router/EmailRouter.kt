package com.lollipop.lqrdemo.router

import android.content.Context
import android.content.Intent
import com.lollipop.qr.comm.BarcodeInfo


object EmailRouter : BarcodeRouter<BarcodeInfo.Email>() {

    override fun getIntent(context: Context, barcodeInfo: BarcodeInfo.Email): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(barcodeInfo.address))
        intent.putExtra(Intent.EXTRA_SUBJECT, barcodeInfo.subject)
        intent.putExtra(Intent.EXTRA_TEXT, barcodeInfo.body)
        return Intent.createChooser(intent, "")
    }

}