package com.lollipop.techo.qr.result

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.databinding.ItemQrResultRootBinding
import com.lollipop.techo.qr.QrCreateActivity

abstract class QrResultRootHolder<B : ViewBinding>(
    protected val binding: ResultItemView<B>
) : RecyclerView.ViewHolder(binding.card.root) {

    companion object {

        inline fun <reified T : ViewBinding> ViewGroup.bindContent(): ResultItemView<T> {
            val rootBinding = bind<ItemQrResultRootBinding>()
            val contentBinding = rootBinding.qrInfoContentView.bind<T>()
            return ResultItemView(contentBinding, rootBinding)
        }

    }

    private var currentInfo: BarcodeInfo? = null

    init {
        binding.card.qrBtn.onClick {
            showQr()
        }
    }

    fun bind(info: BarcodeInfo) {
        currentInfo = info
        onBind(info)
    }

    abstract fun onBind(info: BarcodeInfo)

    protected inline fun <reified T : BarcodeInfo> BarcodeInfo.bindContent(
        block: B.(T) -> Unit
    ) {
        val content = binding.content
        val info = this
        if (info is T) {
            block(content, info)
        }
    }

    protected open fun showQr() {
        val info = currentInfo ?: return
        var format = info.format
        if (format.zxing.isFailure) {
            format = BarcodeFormat.QR_CODE
        }
        QrCreateActivity.start(itemView.context, info.getBarcodeValue(), format)
    }

    class ResultItemView<C : ViewBinding>(
        val content: C,
        val card: ItemQrResultRootBinding
    )

}