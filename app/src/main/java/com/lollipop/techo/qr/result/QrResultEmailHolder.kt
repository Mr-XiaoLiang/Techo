package com.lollipop.techo.qr.result

import android.view.ViewGroup
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.databinding.ItemQrResultEmailBinding

class QrResultEmailHolder(
    binding: ResultItemView<ItemQrResultEmailBinding>
) : QrResultRootHolder<ItemQrResultEmailBinding>(binding) {

    companion object {
        fun create(parent: ViewGroup): QrResultEmailHolder {
            return QrResultEmailHolder(parent.bindContent())
        }
    }

    init {
        binding.content.emailSubjectView.bindClickByCopy {
            typedInfo<BarcodeInfo.Email>()?.subject ?: ""
        }
        binding.content.emailAddressView.bindClickByCopy {
            typedInfo<BarcodeInfo.Email>()?.address ?: ""
        }
        binding.content.emailBodyView.bindClickByCopy {
            typedInfo<BarcodeInfo.Email>()?.body ?: ""
        }
    }

    override fun onBind() {
        val info = typedInfo<BarcodeInfo.Email>() ?: return
        with(binding.content) {
            emailSubjectView.text = info.subject
            emailAddressView.text = info.address
            emailBodyView.text = info.body
        }
    }

}