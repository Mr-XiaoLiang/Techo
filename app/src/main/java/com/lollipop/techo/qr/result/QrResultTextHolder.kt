package com.lollipop.techo.qr.result

import android.view.ViewGroup
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.databinding.ItemQrResultTextBinding

class QrResultTextHolder(
    binding: ResultItemView<ItemQrResultTextBinding>
) : QrResultRootHolder<ItemQrResultTextBinding>(binding) {

    companion object {
        fun create(parent: ViewGroup): QrResultTextHolder {
            return QrResultTextHolder(parent.bindContent())
        }
    }

    init {
        binding.content.textValueView.bindClickByCopy {
            getTextValue()
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
        with(binding.content) {
            textValueView.text = getTextValue()
        }
    }

}