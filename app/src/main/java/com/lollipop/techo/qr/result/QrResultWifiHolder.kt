package com.lollipop.techo.qr.result

import android.view.ViewGroup
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.databinding.ItemQrResultWifiBinding

class QrResultWifiHolder(
    binding: ResultItemView<ItemQrResultWifiBinding>
) : QrResultRootHolder<ItemQrResultWifiBinding>(binding) {

    companion object {
        fun create(parent: ViewGroup): QrResultWifiHolder {
            return QrResultWifiHolder(parent.bindContent())
        }
    }

    init {

    }

    override fun onBind(info: BarcodeInfo) {
        info.bindContent<BarcodeInfo.Wifi> {
            ssidValueView.text = it.ssid
            pwdValueView.text = it.password
        }
    }

}