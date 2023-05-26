package com.lollipop.lqrdemo.router

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.lollipop.base.util.Clipboard
import com.lollipop.lqrdemo.R
import com.lollipop.qr.comm.BarcodeInfo


object WifiRouter : BarcodeRouter<BarcodeInfo.Wifi>() {

    override fun getIntent(context: Context, barcodeInfo: BarcodeInfo.Wifi): Intent {
        Clipboard.copy(context, value = barcodeInfo.password)
        Toast.makeText(
            context,
            context.getString(R.string.copied_wifi, barcodeInfo.ssid),
            Toast.LENGTH_LONG
        ).show()
        return Intent(Settings.ACTION_WIFI_SETTINGS)
    }

}