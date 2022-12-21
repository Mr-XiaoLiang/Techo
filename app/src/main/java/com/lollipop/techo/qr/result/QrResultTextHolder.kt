package com.lollipop.techo.qr.result

import android.view.ViewGroup
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.util.RichTextHelper

class QrResultTextHolder(
    parent: ViewGroup
) : QrResultBaseHolder(parent) {

    companion object {
        fun create(parent: ViewGroup): QrResultTextHolder {
            return QrResultTextHolder(parent)
        }
    }

    private fun getTextValue(): String {
        val info = currentInfo ?: return ""
        return when (info) {
            is BarcodeInfo.Text -> {
                info.value
            }
            is BarcodeInfo.Unknown -> {
                info.value
            }
            is BarcodeInfo.Isbn -> {
                info.value
            }
            is BarcodeInfo.Product -> {
                info.value
            }
            else -> {
                info.rawValue
            }
        }
    }

    override fun onBind() {
        RichTextHelper.startRichFlow()
            .startStackFlow(getTextValue())
            .fontSize(22)
            .onClick { s, _ -> copyValue(s) }
            .commit()
            .intoContent()
    }

}