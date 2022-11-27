package com.lollipop.techo.qr

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.pigment.Pigment
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.BarcodeResult
import com.lollipop.techo.databinding.ActivityQrScanningBinding

class QrScanningActivity : BaseActivity() {

    private val binding: ActivityQrScanningBinding by lazyBind()

    private val qrReaderHelper = BarcodeHelper.createCameraReader(this)

    private val focusAnimationHelper = FocusAnimationHelper {
        binding.focusView
    }

    private val resultBackPressHandler = BackPressHandler(false) {
        resumeScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        resultBackPressHandler.bindTo(this)
        initCamera()
        initView()
    }

    private fun initView() {
        binding.flashBtn.onClick {
            val torch = !qrReaderHelper.torch
            qrReaderHelper.torch = torch
            binding.flashBtn.isChecked = torch
        }
        binding.flashBtn.isChecked = qrReaderHelper.torch

        binding.galleryBtn.onClick {
            Toast.makeText(this, "打开相册", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {
            notifyBackPress()
        }

        binding.resultImageView.setEmptyTouch()

        binding.appBar.fixInsetsByMargin(WindowInsetsHelper.Edge.HEADER)
        binding.flashBtn.fixInsetsByMargin(WindowInsetsHelper.Edge.build {
            bottom = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
        })
        binding.galleryBtn.fixInsetsByMargin(WindowInsetsHelper.Edge.build {
            bottom = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
        })
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.backButtonIcon.imageTintList = ColorStateList.valueOf(pigment.onPrimaryTitle)
        binding.backButtonBackground.setBackgroundColor(pigment.primary)
    }

    private fun initCamera() {
        qrReaderHelper.bindContainer(binding.previewContainer)
        qrReaderHelper.addOnFocusChangedListener(focusAnimationHelper)
        qrReaderHelper.addOnBarcodeScanResultListener(::onResult)
    }

    private fun onResult(result: BarcodeResult) {
        log("OnBarcodeScanResult: ${result.list.size}")
        if (!result.isEmpty) {
            qrReaderHelper.analyzerEnable = false
            qrReaderHelper.analyzerBitmap?.let {
                if (!it.isRecycled) {
                    onUI {
                        binding.resultImageView.setImageBitmap(it)
                        binding.resultImageView.isVisible = true
                    }
                }
            }
            resultBackPressHandler.isEnabled = true

            result.list.forEach {
                log(it.describe.displayValue + ", " + it.info::class.java)
            }
        }
    }

    private fun resumeScan() {
        resultBackPressHandler.isEnabled = false
        binding.resultImageView.setImageDrawable(null)
        binding.resultImageView.isVisible = false
        qrReaderHelper.analyzerEnable = true
    }

}