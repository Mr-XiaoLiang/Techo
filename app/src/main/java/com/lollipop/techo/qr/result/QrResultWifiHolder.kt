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
        binding.content.ssidValueView.bindClickByCopy {
            typedInfo<BarcodeInfo.Wifi>()?.ssid ?: ""
        }
        binding.content.pwdValueView.bindClickByCopy {
            typedInfo<BarcodeInfo.Wifi>()?.password ?: ""
        }
    }

    override fun onBind() {
        val info = typedInfo<BarcodeInfo.Wifi>() ?: return
        with(binding.content) {
            ssidValueView.text = info.ssid
            pwdValueView.text = info.password
        }
    }

}