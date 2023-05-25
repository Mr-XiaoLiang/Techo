package com.lollipop.lqrdemo

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.Sharesheet
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.WindowInsetsType
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.databinding.DialogBarCodeDetailBinding
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentProvider
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper

class BarcodeDetailDialog(
    context: Context,
    private val info: BarcodeWrapper,
    private var openCallback: OpenBarcodeCallback?,
) : BottomSheetDialog(context), PigmentPage {

    companion object {
        fun show(context: Context, info: BarcodeWrapper, openCallback: OpenBarcodeCallback?) {
            BarcodeDetailDialog(context, info, openCallback).show()
        }
    }

    private val binding: DialogBarCodeDetailBinding by lazyBind()

    private val rawValue by lazy {
        try {
            String(info.describe.bytes)
        } catch (e: Throwable) {
            e.printStackTrace()
            info.describe.displayValue
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.let {
            WindowInsetsHelper.fitsSystemWindows(it)
            updateWindowAttributes(it)
        }
        binding.contentLayout.fixInsetsByPadding(WindowInsetsEdge.CONTENT).apply {
            windowInsetsOperator.insetsType = WindowInsetsType.SYSTEM_GESTURES
        }
        binding.contentValueView.text = getBarcodeDisplay()
        binding.hintView.setText(getBarcodeType())
        binding.copyButton.onClick {
            Clipboard.copy(context, value = info.info.rawValue)
            Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.shareButton.onClick {
            Sharesheet.shareText(context, rawValue)
            dismiss()
        }
        binding.openButton.onClick {
            openBarcode()
            dismiss()
        }
        binding.root.post {
            findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)
        }
        findPigmentProvider()?.registerPigment(this)
    }

    override fun dismiss() {
        super.dismiss()
        findPigmentProvider()?.unregisterPigment(this)
    }

    private fun updateWindowAttributes(window: Window) {
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        window.attributes = window.attributes.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private fun openBarcode() {
        if (openCallback != null) {
            openCallback?.openBarcode(info)
            return
        }
        when (val barcodeInfo = info.info) {
            is BarcodeInfo.CalendarEvent -> {
                // TODO()
            }

            is BarcodeInfo.Contact -> {
                openContact(barcodeInfo)
                return
            }

            is BarcodeInfo.DriverLicense -> {
                // TODO()
            }

            is BarcodeInfo.Email -> {
                // TODO()
            }

            is BarcodeInfo.GeoPoint -> {
                // TODO()
            }

            is BarcodeInfo.Isbn -> {
                // TODO()
            }

            is BarcodeInfo.Phone -> {
                // TODO()
            }

            is BarcodeInfo.Product -> {
                // TODO()
            }

            is BarcodeInfo.Sms -> {
                // TODO()
            }

            is BarcodeInfo.Text,
            is BarcodeInfo.Unknown,
            -> {
                tryOpen {
                    binding.shareButton.callOnClick()
                }
            }

            is BarcodeInfo.Url -> {
                tryOpen {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(barcodeInfo.url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                return
            }

            is BarcodeInfo.Wifi -> {
                // TODO()
            }
        }
        // 以上都没做实现，所以先无脑跳
        tryOpen {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawValue))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun openContact(barcodeInfo: BarcodeInfo.Contact) {
        //添加需要设置的数据
        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
        intent.putExtra(
            ContactsContract.Intents.Insert.NAME,
            barcodeInfo.name.getDisplayValue()
        )
        barcodeInfo.phones.findFirst()?.let { phone ->
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone.number)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phone.type.contacts)
        }
        barcodeInfo.phones.findSecondary()?.let { phone ->
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, phone.number)
            intent.putExtra(
                ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                phone.type.contacts
            )
        }
        barcodeInfo.phones.findTertiary()?.let { phone ->
            intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, phone.number)
            intent.putExtra(
                ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE,
                phone.type.contacts
            )
        }
        barcodeInfo.emails.findFirst()?.let { email ->
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email.address)
            intent.putExtra(
                ContactsContract.Intents.Insert.EMAIL_TYPE,
                email.type.contacts
            )
        }
        barcodeInfo.emails.findSecondary()?.let { email ->
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, email.address)
            intent.putExtra(
                ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE,
                email.type.contacts
            )
        }
        barcodeInfo.emails.findTertiary()?.let { email ->
            intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, email.address)
            intent.putExtra(
                ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE,
                email.type.contacts
            )
        }
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, barcodeInfo.organization)

        val values = ArrayList<ContentValues>()

        barcodeInfo.addresses.forEach { address ->
            values.add(ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                val linesValue = StringBuilder()
                address.lines.forEach {
                    linesValue.append(it)
                    linesValue.append(" ")
                }
                put(
                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                    linesValue.toString()
                )
            })
        }
        barcodeInfo.urls.forEach { url ->
            values.add(ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                )
                put(ContactsContract.CommonDataKinds.Website.URL, url)
            })
        }
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, values)
        context.startActivity(intent)
    }

    private inline fun <reified T : Any> List<T>.findFirst(): T? {
        if (isEmpty()) {
            return null
        }
        return this[0]
    }

    private inline fun <reified T : Any> List<T>.findSecondary(): T? {
        if (size < 2) {
            return null
        }
        return this[1]
    }

    private inline fun <reified T : Any> List<T>.findTertiary(): T? {
        if (size < 3) {
            return null
        }
        return this[2]
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

    private fun findPigmentProvider(): PigmentProvider? {
        var c = context
        while (true) {
            if (c is PigmentProvider) {
                return c
            }
            if (c is ContextWrapper) {
                c = c.baseContext
            } else {
                return null
            }
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        binding.contentLayout.setBackgroundColor(pigment.backgroundColor)
        binding.dialogTouchHolder.color = pigment.onBackgroundBody
        binding.hintView.setTextColor(pigment.onBackgroundTitle)
        binding.contentValueView.setTextColor(pigment.onBackgroundBody)
        binding.shareButton.setTextColor(pigment.onBackgroundTitle)
        binding.shareButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.copyButton.setTextColor(pigment.onBackgroundTitle)
        binding.copyButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.openButton.setTextColor(pigment.onBackgroundTitle)
        binding.openButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
    }

    fun interface OpenBarcodeCallback {
        fun openBarcode(info: BarcodeWrapper)
    }

}