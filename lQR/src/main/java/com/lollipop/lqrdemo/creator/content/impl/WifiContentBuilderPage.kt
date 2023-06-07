package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class WifiContentBuilderPage : ContentBuilder() {

    private var password by remember()
    private var ssid by remember()
    private var username by remember()

    override fun getContentValue(): String {
        val wifi = BarcodeInfo.Wifi()
        wifi.encryptionType = if (password.isEmpty()) {
            BarcodeInfo.Wifi.EncryptionType.OPEN
        } else {
            BarcodeInfo.Wifi.EncryptionType.WPA
        }
        wifi.ssid = ssid
        wifi.username = username
        return wifi.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                context.getString(R.string.wifi_ssid),
                InputConfig.NORMAL,
                { ssid },
            ) {
                ssid = it
            }
            Input(
                context.getString(R.string.wifi_user),
                InputConfig.NORMAL,
                { username },
            ) {
                username = it
            }
            Input(
                context.getString(R.string.wifi_pwd),
                InputConfig.NORMAL,
                { password },
            ) {
                password = it
            }
            SpaceEnd()
        }
    }
}