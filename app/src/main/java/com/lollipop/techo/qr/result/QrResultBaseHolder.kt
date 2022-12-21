package com.lollipop.techo.qr.result

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ItemQrResultBinding
import com.lollipop.techo.qr.QrCreateActivity
import com.lollipop.techo.util.RichTextHelper

abstract class QrResultBaseHolder private constructor(
    protected val binding: ItemQrResultBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(parent.bind<ItemQrResultBinding>())

    protected var currentInfo: BarcodeInfo? = null
        private set

    init {
        binding.qrBtn.onClick {
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

    protected fun setContentInfo(value: CharSequence) {
        binding.qrInfoContentView.text = value
    }

    protected fun RichTextHelper.intoContent() {
        val helper = this@intoContent
        setContentInfo(helper.build())
    }

    protected fun getColor(colorId: Int): Int {
        return itemView.context.getColor(colorId)
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
        onClick {
            copyValue(value())
        }
    }

    protected fun copyValue(value: String) {
        Clipboard.copy(itemView.context, value = value)
        Toast.makeText(itemView.context, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    protected fun RichTextHelper.addInfo(
        value: String,
        color: Int,
        size: Int,
        flag: String = "QR"
    ): RichTextHelper {
        return usStr(value) {
            addClickInfo(
                it,
                color,
                it,
                RichTextHelper.ClickEvent.Custom(flag)
            ) { info, _ ->
                copyValue(info)
            }
        }
    }

}