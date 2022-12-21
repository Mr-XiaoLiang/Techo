package com.lollipop.techo.qr.result

import android.view.ViewGroup
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.util.RichTextHelper

class QrResultWifiHolder(
    parent: ViewGroup
) : QrResultBaseHolder(parent) {

    companion object {
        fun create(parent: ViewGroup): QrResultWifiHolder {
            return QrResultWifiHolder(parent)
        }
    }

    override fun onBind() {
        val info = typedInfo<BarcodeInfo.Wifi>() ?: return
        RichTextHelper.startRichFlow()
            .startStackFlow(info.ssid)
            .fontSize(26)
            .onClick { s, _ -> copyValue(s) }
            .commit()
            .addInfo("\n")
            .startStackFlow(info.password)
            .fontSize(16)
            .onClick { s, _ -> copyValue(s) }
            .commit()
            .intoContent()
    }

}