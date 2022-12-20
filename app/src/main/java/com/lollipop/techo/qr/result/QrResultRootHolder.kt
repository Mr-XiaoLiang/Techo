package com.lollipop.techo.qr.result

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.R
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

    protected var currentInfo: BarcodeInfo? = null
        private set

    init {
        binding.card.qrBtn.onClick {
            showQr()
        }
    }

    fun bind(info: BarcodeInfo) {
        currentInfo = info
        onBind()
    }

    protected inline fun <reified T : BarcodeInfo> typedInfo(): T? {
        val info = currentInfo
        if (info is T) {
            return info
        }
        return null
    }

    protected abstract fun onBind()

    protected open fun showQr() {
        val info = currentInfo ?: return
        var format = info.format
        if (format.zxing.isFailure) {
            format = BarcodeFormat.QR_CODE
        }
        QrCreateActivity.start(itemView.context, info.getBarcodeValue(), format)
    }

    protected fun View.bindClickByCopy(value: () -> String) {
        Clipboard.copy(itemView.context, value = value())
        Toast.makeText(itemView.context, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    class ResultItemView<C : ViewBinding>(
        val content: C,
        val card: ItemQrResultRootBinding
    )

}