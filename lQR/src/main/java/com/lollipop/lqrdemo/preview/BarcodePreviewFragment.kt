package com.lollipop.lqrdemo.preview

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.util.Clipboard
import com.lollipop.base.util.ShareSheet
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.onClick
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
import kotlin.math.abs

class BarcodePreviewFragment : BaseFragment() {

    private var cardAnimationHelper: CardAnimationHelper? = null
    private var binding: FragmentBarcodePreviewBinding? = null
    private var openCallback: OpenBarcodeCallback? = null

    private val barcodeInfoDelegate = BarcodeDelegate()

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
            newBinding.infoCard
        )
        return newBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardAnimationHelper?.preload()
        binding?.apply {
            closeBtn.setOnClickListener { hide() }
            backgroundView.setOnClickListener { hide() }
            touchHoldView.setOnTouchListener { _, _ -> true }

            openButton.onClick {
                openBarcode()
                hide()
            }

            charsetButton.onClick {
                showCharsetPopMenu()
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
        }

        updateCharsetButton()
    }

    fun show(barcode: BarcodeWrapper) {
        onBarcodeUpdate(barcode)
        binding?.touchHoldView?.isVisible = true
        cardAnimationHelper?.show()
        // TODO
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

    fun hide() {
        binding?.touchHoldView?.isVisible = false
        cardAnimationHelper?.hide()
        // TODO
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
        binding?.contentValueView?.text = barcodeInfoDelegate.decodeValue
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
        val infoCard: View
    ) {

        companion object {
            private const val ANIMATION_DURATION = 300L
            private const val PROGRESS_MIN = 0F
            private const val PROGRESS_MAX = 1F
            private const val PROGRESS_DELTA = PROGRESS_MAX - PROGRESS_MIN
            private const val PROGRESS_CLOSE_THRESHOLD = PROGRESS_MIN + 0.01F
            private const val PROGRESS_OPEN_THRESHOLD = PROGRESS_MAX - 0.01F
        }

        private val valueAnimator = ValueAnimator()
        private var animationProgress = PROGRESS_MIN

        val isClosed: Boolean
            get() {
                return animationProgress <= PROGRESS_CLOSE_THRESHOLD
            }
        val isOpened: Boolean
            get() {
                return animationProgress >= PROGRESS_OPEN_THRESHOLD
            }

        init {
            valueAnimator.addUpdateListener(UpdateListener(::onAnimationUpdate))
            valueAnimator.addListener(
                StateListener(
                    onStart = ::onAnimationStart,
                    onEnd = ::onAnimationEnd
                )
            )
        }

        fun preload() {
            backgroundView.isInvisible = true
            previewCard.isInvisible = true
            infoCard.isInvisible = true
            backgroundView.post {
                onAnimationUpdate(PROGRESS_MIN)
                onAnimationEnd()
            }
        }

        fun show() {
            animationTo(PROGRESS_MAX)
        }

        fun hide() {
            animationTo(PROGRESS_MIN)
        }

        private fun animationTo(target: Float) {
            valueAnimator.cancel()
            val duration = abs(animationProgress - target) / PROGRESS_DELTA * ANIMATION_DURATION
            valueAnimator.duration = duration.toLong()
            valueAnimator.setFloatValues(animationProgress, target)
            valueAnimator.start()
        }

        private fun onAnimationUpdate(value: Float) {
            animationProgress = value
            backgroundView.alpha = value
            previewCard.alpha = value
            infoCard.alpha = value
            val offsetProgress = (PROGRESS_MAX - value + PROGRESS_MIN) / PROGRESS_DELTA
            infoCard.translationY = infoCard.height * offsetProgress
        }

        private fun onAnimationStart() {
            backgroundView.isVisible = true
            previewCard.isVisible = true
            infoCard.isVisible = true
        }

        private fun onAnimationEnd() {
            if (isClosed) {
                backgroundView.isInvisible = true
                previewCard.isInvisible = true
                infoCard.isInvisible = true
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

    private class UpdateListener(
        private val onUpdate: (Float) -> Unit
    ) : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val value = animation.animatedValue
            if (value is Float) {
                onUpdate(value)
            }
        }
    }

    private class StateListener(
        private val onStart: () -> Unit,
        private val onEnd: () -> Unit
    ) : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            onStart()
        }

        override fun onAnimationEnd(animation: Animator) {
            onEnd()
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    }

    fun interface OpenBarcodeCallback {
        fun openBarcode(info: BarcodeWrapper): Boolean
    }

}