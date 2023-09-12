package com.lollipop.lqrdemo

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.ShareSheet
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.base.BaseBottomDialog
import com.lollipop.lqrdemo.databinding.DialogBarCodeDetailBinding
import com.lollipop.lqrdemo.other.CharsetMenuDialog
import com.lollipop.lqrdemo.router.CalendarEventRouter
import com.lollipop.lqrdemo.router.ContactRouter
import com.lollipop.lqrdemo.router.EmailRouter
import com.lollipop.lqrdemo.router.GeoRouter
import com.lollipop.lqrdemo.router.PhoneRouter
import com.lollipop.lqrdemo.router.SmsRouter
import com.lollipop.lqrdemo.router.WifiRouter
import com.lollipop.pigment.Pigment
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper
import java.nio.charset.Charset

class BarcodeDetailDialog(
    context: Context,
    private val info: BarcodeWrapper,
    private var openCallback: OpenBarcodeCallback?,
) : BaseBottomDialog(context) {

    companion object {
        fun show(context: Context, info: BarcodeWrapper, openCallback: OpenBarcodeCallback?) {
            BarcodeDetailDialog(context, info, openCallback).show()
        }
    }

    private val binding: DialogBarCodeDetailBinding by lazyBind()

    private var charset: Charset? = null

    private val rawValue by lazy {
        try {
            String(info.describe.bytes)
        } catch (e: Throwable) {
            e.printStackTrace()
            info.describe.displayValue
        }
    }
    override val contentView: View
        get() = binding.root

    private var decodeValue: String = ""

    private fun getRaw(): String {
        if (decodeValue.isNotEmpty()) {
            return decodeValue
        }
        return rawValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.contentValueView.text = getBarcodeDisplay()
        binding.hintView.setText(getBarcodeType())
        binding.copyButton.onClick {
            Clipboard.copy(context, value = getRaw())
            Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.shareButton.onClick {
            ShareSheet.shareText(context, getRaw())
            dismiss()
        }
        binding.openButton.onClick {
            openBarcode()
            dismiss()
        }

        binding.charsetButton.onClick {
            showCharsetPopMenu()
        }
        updateCharsetButton()
    }

    private fun updateCharsetButton() {
        val c = charset
        binding.charsetButton.text = if (c == null) {
            context.getString(R.string.garbled)
        } else {
            c.name()
        }
    }

    private fun updateCharset(c: Charset) {
        charset = c
        // 修改 rawValue 和 displayValue 的内容吧
        val newValue = String(info.describe.bytes, c)
        decodeValue = newValue
        binding.contentValueView.text = newValue
        updateCharsetButton()
    }

    private fun showCharsetPopMenu() {
        CharsetMenuDialog(context) {
            updateCharset(it)
        }.show()
    }

    private fun openBarcode() {
        if (openCallback != null) {
            openCallback?.openBarcode(info)
            return
        }
        when (val barcodeInfo = info.info) {
            is BarcodeInfo.CalendarEvent -> {
                if (CalendarEventRouter.open(context, barcodeInfo)) {
                    return
                }
            }

            is BarcodeInfo.Contact -> {
                if (ContactRouter.open(context, barcodeInfo)) {
                    return
                }
            }

            is BarcodeInfo.DriverLicense -> {
                // TODO()
            }

            is BarcodeInfo.Email -> {
                if (EmailRouter.open(context, barcodeInfo)) {
                    return
                }
            }

            is BarcodeInfo.GeoPoint -> {
                if (GeoRouter.open(context, barcodeInfo)) {
                    return
                }
            }

            is BarcodeInfo.Isbn -> {
                // TODO()
            }

            is BarcodeInfo.Phone -> {
                if (PhoneRouter.open(context, barcodeInfo)) {
                    return
                }
            }

            is BarcodeInfo.Product -> {
                // TODO()
            }

            is BarcodeInfo.Sms -> {
                if (SmsRouter.open(context, barcodeInfo)) {
                    return
                }
            }

            is BarcodeInfo.Text,
            is BarcodeInfo.Unknown,
            -> {
                tryOpen {
                    openByViewAction(getRaw())
                }
                return
            }

            is BarcodeInfo.Url -> {
                tryOpen {
                    openByViewAction(barcodeInfo.url)
                }
                return
            }

            is BarcodeInfo.Wifi -> {
                if (WifiRouter.open(context, barcodeInfo)) {
                    return
                }
            }
        }
        // 以上都没做实现，所以先无脑跳
        tryOpen {
            openByViewAction(getRaw())
        }
    }

    private fun openByViewAction(raw: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(raw))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun tryOpen(callback: () -> Unit) {
        try {
            callback()
        } catch (e: Throwable) {
            e.printStackTrace()
            binding.shareButton.callOnClick()
        }
    }

    private fun getBarcodeType(): Int {
        return when (info.info) {
            is BarcodeInfo.CalendarEvent -> R.string.code_calendar_event
            is BarcodeInfo.Contact -> R.string.code_contact
            is BarcodeInfo.DriverLicense -> R.string.code_driver_license
            is BarcodeInfo.Email -> R.string.code_email
            is BarcodeInfo.GeoPoint -> R.string.code_geo_point
            is BarcodeInfo.Isbn -> R.string.code_isbn
            is BarcodeInfo.Phone -> R.string.code_phone
            is BarcodeInfo.Product -> R.string.code_product
            is BarcodeInfo.Sms -> R.string.code_sms
            is BarcodeInfo.Text,
            is BarcodeInfo.Unknown,
            -> R.string.code_text

            is BarcodeInfo.Url -> R.string.code_url
            is BarcodeInfo.Wifi -> R.string.code_wifi
        }
    }

    private fun getBarcodeDisplay(): String {
        when (val barcodeInfo = info.info) {
            is BarcodeInfo.CalendarEvent -> {}
            is BarcodeInfo.Contact -> {
                val builder = StringBuilder()
                builder.append(barcodeInfo.name.getDisplayValue())
                barcodeInfo.phones.forEach {
                    builder.append("\n")
                    builder.append(it.number)
                }
                return builder.toString()
            }

            is BarcodeInfo.DriverLicense -> {}
            is BarcodeInfo.Email -> {}
            is BarcodeInfo.GeoPoint -> {}
            is BarcodeInfo.Isbn -> {}
            is BarcodeInfo.Phone -> {}
            is BarcodeInfo.Product -> {}
            is BarcodeInfo.Sms -> {}
            is BarcodeInfo.Text,
            is BarcodeInfo.Unknown,
            -> {
            }

            is BarcodeInfo.Url -> {}
            is BarcodeInfo.Wifi -> {}
        }
        return info.describe.displayValue
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.hintView.setTextColor(pigment.onBackgroundTitle)
        binding.contentValueView.setTextColor(pigment.onBackgroundBody)
        binding.shareButton.setTextColor(pigment.onBackgroundTitle)
        binding.shareButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.copyButton.setTextColor(pigment.onBackgroundTitle)
        binding.copyButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.openButton.setTextColor(pigment.onBackgroundTitle)
        binding.openButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.charsetButton.setBackgroundColor(pigment.primaryColor.changeAlpha(0.25F))
        binding.charsetButton.setTextColor(pigment.onBackgroundBody)
    }

    fun interface OpenBarcodeCallback {
        fun openBarcode(info: BarcodeWrapper)
    }

}