package com.lollipop.techo.qr.result

import android.view.ViewGroup
import android.widget.Toast
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.onClick
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ItemQrResultWifiBinding

class QrResultWifiHolder(
    binding: ResultItemView<ItemQrResultWifiBinding>
) : QrResultRootHolder<ItemQrResultWifiBinding>(binding) {

    companion object {
        fun create(parent: ViewGroup): QrResultWifiHolder {
            return QrResultWifiHolder(parent.bindContent())
        }
    }

    private var ssidValue = ""
    private var passwordValue = ""

    init {
        binding.content.ssidValueView.onClick {
            onSsidViewClick()
        }
        binding.content.pwdValueView.onClick {
            onPwdViewClick()
        }
    }

    private fun onSsidViewClick() {
        Clipboard.copy(itemView.context, value = ssidValue)
        Toast.makeText(itemView.context, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    private fun onPwdViewClick() {
        Clipboard.copy(itemView.context, value = passwordValue)
        Toast.makeText(itemView.context, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    override fun onBind(info: BarcodeInfo) {
        info.bindContent<BarcodeInfo.Wifi> {
            ssidValue = it.ssid
            passwordValue = it.password
            ssidValueView.text = it.ssid
            pwdValueView.text = it.password
        }
    }

}