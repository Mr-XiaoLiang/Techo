package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class WifiContentBuilderPage : ContentBuilder() {

    private var encryptionType: BarcodeInfo.Wifi.EncryptionType = BarcodeInfo.Wifi.EncryptionType.OPEN
    private var password by remember()
    private var ssid by remember()
    private var username by remember()

    override fun getContentValue(): String {
        val wifi = BarcodeInfo.Wifi()
//        TODO("Not yet implemented")
        return wifi.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
//        TODO("Not yet implemented")
    }
}