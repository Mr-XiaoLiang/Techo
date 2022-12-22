package com.lollipop.techo.qr.result

import android.view.ViewGroup
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.R
import com.lollipop.techo.util.RichTextHelper

class QrResultContactHolder(
    parent: ViewGroup
) : QrResultBaseHolder(parent) {

    companion object {
        fun create(parent: ViewGroup): QrResultContactHolder {
            return QrResultContactHolder(parent)
        }
    }

    override fun onBind() {
        val info = typedInfo<BarcodeInfo.Contact>()
        if (info == null) {
            setContentInfo("")
            return
        }
        val helper = RichTextHelper.startRichFlow()
            .addLine(info.name.getDisplayValue(), R.color.text_gray_10, 18, false)
            .addLine(info.title, R.color.text_gray_8, 16)
            .addLine(info.organization, R.color.text_gray_8, 16)
        info.phones.forEach {
            helper.addLine(it.number, R.color.text_gray_9, 16)
        }
        info.emails.forEach {
            helper.addLine(it.address, R.color.text_gray_9, 16)
        }
        info.urls.forEach {
            helper.addLine(it, R.color.text_gray_9, 16)
        }
        info.addresses.forEach {
            helper.addLine(it.lines.displayValue(), R.color.text_gray_8, 16)
        }
    }

    private fun Array<String>.displayValue(): String {
        val array = this
        if (array.isEmpty()) {
            return ""
        }
        if (array.size == 1) {
            return array[0]
        }
        val builder = StringBuilder()
        array.forEach {
            if (builder.isNotEmpty()) {
                builder.append(" ")
            }
            builder.append(it)
        }
        return builder.toString()
    }

    private fun RichTextHelper.addLine(
        info: String,
        colorId: Int,
        fontSize: Int,
        newLine: Boolean = true
    ): RichTextHelper {
        if (info.isEmpty()) {
            return this
        }
        if (newLine) {
            addInfo("\n")
        }
        return startStackFlow(info)
            .color(itemView.context.getColor(colorId))
            .fontSize(fontSize)
            .onClick { s, _ -> copyValue(s) }
            .commit()
    }

}