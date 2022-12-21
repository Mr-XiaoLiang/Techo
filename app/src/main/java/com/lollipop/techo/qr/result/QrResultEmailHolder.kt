package com.lollipop.techo.qr.result

import android.view.ViewGroup
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.R
import com.lollipop.techo.util.RichTextHelper

class QrResultEmailHolder(
    parent: ViewGroup
) : QrResultBaseHolder(parent) {

    companion object {
        fun create(parent: ViewGroup): QrResultEmailHolder {
            return QrResultEmailHolder(parent)
        }
    }

    override fun onBind() {
        val info = typedInfo<BarcodeInfo.Email>() ?: return
        RichTextHelper.startRichFlow()
            .startStackFlow(info.subject)
            .fontSize(18)
            .textStyle(bold = true, italic = false)
            .color(getColor(R.color.text_gray_10))
            .onClick { s, _ -> copyValue(s) }
            .commit()
            .usStr(info.address) {
                addInfo("\n")
                startStackFlow(it)
                    .fontSize(14)
                    .textStyle(bold = false, italic = true)
                    .color(getColor(R.color.text_gray_9))
                    .onClick { s, _ -> copyValue(s) }
                    .commit()
            }
            .usStr(info.body) {
                addInfo("\n")
                startStackFlow(it)
                    .fontSize(14)
                    .color(getColor(R.color.text_gray_7))
                    .onClick { s, _ -> copyValue(s) }
                    .commit()
            }
            .intoContent()
    }

}