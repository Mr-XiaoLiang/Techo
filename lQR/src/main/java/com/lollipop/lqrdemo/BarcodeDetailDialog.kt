package com.lollipop.lqrdemo

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.Sharesheet
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.databinding.DialogBarCodeDetailBinding
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper

class BarcodeDetailDialog(
    context: Context, private val info: BarcodeWrapper
) : BottomSheetDialog(context) {

    companion object {
        fun show(context: Context, info: BarcodeWrapper) {
            BarcodeDetailDialog(context, info).show()
        }
    }

    private val binding: DialogBarCodeDetailBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.let {
            WindowInsetsHelper.initWindowFlag(it)
        }
        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.CONTENT)

        val barcodeValue = info.describe.displayValue
        binding.contentValueView.text = barcodeValue
        binding.hintView.setText(getBarcodeType())
        binding.copyButton.onClick {
            Clipboard.copy(context, value = barcodeValue)
            Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.shareButton.onClick {
            Sharesheet.shareText(context, String(info.describe.bytes))
            dismiss()
        }
        binding.openButton.onClick {
//            openBarcode()
            dismiss()
        }
    }

    private fun openBarcode() {
        when (info.info) {
            is BarcodeInfo.CalendarEvent -> TODO()
            is BarcodeInfo.Contact -> TODO()
            is BarcodeInfo.DriverLicense -> TODO()
            is BarcodeInfo.Email -> TODO()
            is BarcodeInfo.GeoPoint -> TODO()
            is BarcodeInfo.Isbn -> TODO()
            is BarcodeInfo.Phone -> TODO()
            is BarcodeInfo.Product -> TODO()
            is BarcodeInfo.Sms -> TODO()
            is BarcodeInfo.Text,
            is BarcodeInfo.Unknown -> TODO()

            is BarcodeInfo.Url -> TODO()
            is BarcodeInfo.Wifi -> TODO()
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
            is BarcodeInfo.Unknown -> R.string.code_text

            is BarcodeInfo.Url -> R.string.code_url
            is BarcodeInfo.Wifi -> R.string.code_wifi
        }
    }

}