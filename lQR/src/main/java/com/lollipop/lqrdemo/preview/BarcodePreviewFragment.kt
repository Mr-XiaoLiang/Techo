package com.lollipop.lqrdemo.preview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.ShareSheet
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.onClick
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.FragmentBarcodePreviewBinding
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

class BarcodePreviewFragment : BaseFragment() {

    private var cardAnimationHelper: CardAnimationHelper? = null
    private var binding: FragmentBarcodePreviewBinding? = null
    private var openCallback: OpenBarcodeCallback? = null

    private val barcodeInfoDelegate = BarcodeDelegate()
    private val previewProvider = BarcodePreviewProvider()
    private var barcodeInfo: BarcodeWrapper? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        openCallback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        openCallback = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val newBinding = FragmentBarcodePreviewBinding.inflate(inflater, container, false)
        binding = newBinding
        cardAnimationHelper = CardAnimationHelper(
            newBinding.backgroundView,
            newBinding.previewCard,
            newBinding.infoCard,
            ::onSheetHide
        )
        return newBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardAnimationHelper?.preload()
        binding?.apply {
            touchHoldView.isVisible = false
            backgroundView.setOnClickListener { hide() }
            touchHoldView.setOnTouchListener { _, _ -> true }

            openButton.onClick {
                openBarcode()
                hide()
            }

            charsetButton.onClick {
                showCharsetPopMenu()
            }
            infoCard.onClick {
                // 设置一个点击事件拦截
            }
            copyButton.onClick {
                context?.let {
                    Clipboard.copy(it, value = barcodeInfoDelegate.decodeValue)
                    Toast.makeText(it, R.string.copied, Toast.LENGTH_SHORT).show()
                }
                hide()
            }

            shareButton.onClick {
                context?.let {
                    ShareSheet.shareText(it, barcodeInfoDelegate.decodeValue)
                }
                hide()
            }
            infoCard.fixInsetsByPadding(WindowInsetsEdge.BOTTOM)
            cardGroup.fixInsetsByPadding(WindowInsetsEdge.ALL)
        }

        updateCharsetButton()
    }

    fun show(barcode: BarcodeWrapper) {
        onBarcodeUpdate(barcode)
        val hasPreview = previewProvider.hasPreview(barcode)
        cardAnimationHelper?.previewVisibility = hasPreview
        binding?.apply {
            if (hasPreview) {
                previewProvider.showPreview(barcode, previewCard)
                currentPigment?.let {
                    previewProvider.onDecorationChanged(it)
                }
            } else {
                previewProvider.clearPreview(previewCard)
            }
            touchHoldView.isVisible = true
        }
        cardAnimationHelper?.show()
    }

    fun testShow() {
        binding?.touchHoldView?.isVisible = true
        cardAnimationHelper?.show()
    }

    private fun onBarcodeUpdate(barcode: BarcodeWrapper) {
        this.barcodeInfo = barcode
        // 更新记录
        barcodeInfoDelegate.update(barcode)
        // 更新UI
        binding?.apply {
            hintView.setText(getBarcodeType(barcode))
        }
        updateInfoContent()
        updateCharsetButton()
    }

    private fun onSheetHide() {
        binding?.apply {
            previewProvider.clearPreview(previewCard)
            touchHoldView.isVisible = false
        }
    }

    fun hide() {
        cardAnimationHelper?.hide()
    }

    private fun updateCharsetButton() {
        val c = barcodeInfoDelegate.charset
        binding?.apply {
            if (c == null) {
                charsetButton.setText(R.string.garbled)
            } else {
                charsetButton.text = c.name()
            }
        }
    }

    private fun updateCharset(c: Charset) {
        barcodeInfoDelegate.update(c)
        // 修改 rawValue 和 displayValue 的内容吧
        updateInfoContent()
        updateCharsetButton()
    }

    private fun updateInfoContent() {
        binding?.contentValueView?.text = barcodeInfoDelegate.displayValue
    }

    private fun showCharsetPopMenu() {
        context?.let {
            CharsetMenuDialog(it) {
                updateCharset(it)
            }.show()
        }
    }

    private fun openBarcode() {
        val info = barcodeInfo ?: return
        val c = openCallback
        if (c != null && c.openBarcode(info)) {
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
                    openByViewAction(barcodeInfoDelegate.displayValue)
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
            openByViewAction(barcodeInfoDelegate.displayValue)
        }
    }

    private fun openByViewAction(raw: String) {
        val intent = Intent(Intent.ACTION_VIEW, raw.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    private fun tryOpen(callback: () -> Unit) {
        try {
            callback()
        } catch (e: Throwable) {
            e.printStackTrace()
            binding?.shareButton?.callOnClick()
        }
    }

    private fun getBarcodeType(info: BarcodeWrapper): Int {
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

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        previewProvider.onDecorationChanged(pigment)
        binding?.apply {
            previewCard.setBackgroundColor(pigment.backgroundColor)
            infoCard.setBackgroundColor(pigment.backgroundColor)
            hintView.setTextColor(pigment.onBackgroundTitle)
            contentValueView.setTextColor(pigment.onBackgroundBody)
            shareButton.setTextColor(pigment.onBackgroundTitle)
            shareButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
            copyButton.setTextColor(pigment.onBackgroundTitle)
            copyButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
            openButton.setTextColor(pigment.onBackgroundTitle)
            openButton.iconTint = ColorStateList.valueOf(pigment.onBackgroundTitle)
            charsetButton.setBackgroundColor(pigment.primaryColor.changeAlpha(0.25F))
            charsetButton.setTextColor(pigment.onBackgroundBody)
        }
    }

    private class CardAnimationHelper(
        val backgroundView: View,
        val previewCard: View,
        val infoCard: View,
        val onSheetHideCallback: () -> Unit
    ) {

        private val bottomSheetBehavior = BottomSheetBehavior.from<View>(infoCard)

        var previewVisibility = true

        init {
            bottomSheetBehavior.addBottomSheetCallback(
                BottomSheetCallback(
                    ::onSheetHide,
                    ::onSheetSlide
                )
            )
        }

        private fun onSheetHide(isHide: Boolean) {
            if (isHide) {
                onSheetHideCallback()
            }
            backgroundView.isInvisible = isHide
            previewCard.isInvisible = isHide || !previewVisibility
        }

        private fun onSheetSlide(slideOffset: Float) {
            if (slideOffset > 0) {
                onAnimationUpdate(1F)
            } else if (slideOffset < 0) {
                onAnimationUpdate(slideOffset + 1)
            }
        }

        private fun onAnimationUpdate(value: Float) {
            backgroundView.alpha = value
            if (previewCard.isVisible) {
                previewCard.alpha = value
            }
        }

        fun preload() {
            hide()
            onSheetHide(true)
        }

        fun show() {
            // 显示预览
            previewCard.isInvisible = !previewVisibility
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        fun hide() {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        private class BottomSheetCallback(
            val onSheetHide: (Boolean) -> Unit,
            val onSheetSlide: (Float) -> Unit
        ) : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.e("Lollipop", "onStateChanged.newState: $newState")
                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    onSheetHide(true)
                } else {
                    onSheetHide(false)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.e("Lollipop", "onStateChanged.onSlide: $slideOffset")
                onSheetSlide(slideOffset)
            }

        }

    }

    private class BarcodeDelegate {

        var rawValue = ""
            private set
        var decodeValue = ""
            private set
        var charset: Charset? = null
            private set

        val displayValue: String
            get() {
                if (decodeValue.isNotEmpty()) {
                    return decodeValue
                }
                return rawValue
            }

        private var originBytes = ByteArray(0)

        fun update(info: BarcodeWrapper) {
            originBytes = info.describe.bytes
            rawValue = try {
                String(originBytes, Charsets.UTF_8)
            } catch (e: Throwable) {
                e.printStackTrace()
                info.describe.displayValue
            }
            decodeValue = getDecodeValue(charset)
        }

        fun update(charset: Charset) {
            this.charset = charset
            decodeValue = getDecodeValue(charset)
        }

        private fun getDecodeValue(c: Charset?): String {
            c ?: return ""
            return String(originBytes, c)
        }

    }

    fun interface OpenBarcodeCallback {
        fun openBarcode(info: BarcodeWrapper): Boolean
    }

}