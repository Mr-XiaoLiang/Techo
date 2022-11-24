package com.lollipop.techo.qr

import android.os.Bundle
import android.widget.Toast
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.BarcodeResult
import com.lollipop.techo.databinding.ActivityQrScanningBinding

class QrScanningActivity : BaseActivity() {

    private val binding: ActivityQrScanningBinding by lazyBind()

    private val qrReaderHelper = BarcodeHelper.createCameraReader(this)

    private val focusAnimationHelper = FocusAnimationHelper {
        binding.focusView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
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
            onBackPressed()
        }
        binding.appBar.fixInsetsByMargin(WindowInsetsHelper.Edge.HEADER)
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
            result.list.forEach {
                log(it.describe.displayValue + ", " + it.info::class.java)
            }
        }
    }


}