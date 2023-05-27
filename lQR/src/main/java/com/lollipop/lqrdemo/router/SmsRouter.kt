package com.lollipop.lqrdemo.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lollipop.qr.comm.BarcodeInfo


object SmsRouter : BarcodeRouter<BarcodeInfo.Sms>() {

    override fun getIntent(context: Context, barcodeInfo: BarcodeInfo.Sms): Intent {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${barcodeInfo.phoneNumber}"))
        intent.putExtra("sms_body", barcodeInfo.message)
        return intent
    }

}