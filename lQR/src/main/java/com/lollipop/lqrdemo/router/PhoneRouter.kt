package com.lollipop.lqrdemo.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lollipop.qr.comm.BarcodeInfo

object PhoneRouter : BarcodeRouter<BarcodeInfo.Phone>() {

    override fun getIntent(context: Context, barcodeInfo: BarcodeInfo.Phone): Intent {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.setData(Uri.parse("tel:" + barcodeInfo.number))
        return intent
    }

}