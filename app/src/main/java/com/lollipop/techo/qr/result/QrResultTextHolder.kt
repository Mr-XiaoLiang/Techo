package com.lollipop.techo.qr.result

import android.view.ViewGroup
import android.widget.Toast
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.onClick
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ItemQrResultTextBinding

class QrResultTextHolder(
    binding: ResultItemView<ItemQrResultTextBinding>
) : QrResultRootHolder<ItemQrResultTextBinding>(binding) {

    companion object {
        fun create(parent: ViewGroup): QrResultTextHolder {
            return QrResultTextHolder(parent.bindContent())
        }
    }

    private var textValue = ""

    init {
        binding.content.textValueView.onClick {
            onTextViewClick()
        }
    }

    private fun onTextViewClick() {
        Clipboard.copy(itemView.context, value = textValue)
        Toast.makeText(itemView.context, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    override fun onBind(info: BarcodeInfo) {
        info.bindContent<BarcodeInfo.Text> {
            textValue = it.value
            textValueView.text = it.value
        }
        info.bindContent<BarcodeInfo.Unknown> {
            textValue = it.value
            textValueView.text = it.value
        }
        info.bindContent<BarcodeInfo.Isbn> {
            textValue = it.value
            textValueView.text = it.value
        }
        info.bindContent<BarcodeInfo.Product> {
            textValue = it.value
            textValueView.text = it.value
        }
    }

}