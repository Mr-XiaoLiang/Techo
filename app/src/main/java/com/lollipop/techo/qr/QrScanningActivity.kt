package com.lollipop.techo.qr

import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.core.view.isVisible
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.comm.BarcodeResult
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.techo.databinding.ActivityQrScanningBinding

class QrScanningActivity : BaseActivity(), CodeSelectionView.OnCodeSelectedListener {

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

        binding.resultImageView.addOnCodeSelectedListener(this)

        binding.appBar.fixInsetsByMargin(WindowInsetsHelper.Edge.HEADER)
        binding.flashBtn.fixInsetsByMargin(WindowInsetsHelper.Edge.build {
            bottom = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
        })
        binding.galleryBtn.fixInsetsByMargin(WindowInsetsHelper.Edge.build {
            bottom = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
        })
    }

    private fun initCamera() {
        qrReaderHelper.bindContainer(binding.previewContainer)
        qrReaderHelper.addOnFocusChangedListener(focusAnimationHelper)
        qrReaderHelper.addOnBarcodeScanResultListener(::onResult)
    }

    private fun onResult(result: BarcodeResult) {
        if (!result.isEmpty) {
            qrReaderHelper.analyzerEnable = false
            qrReaderHelper.analyzerBitmap?.let {
                if (!it.isRecycled) {
                    onUI {
                        binding.resultImageView.setImageBitmap(it)
                        binding.resultImageView.onCodeResult(
                            Size(it.width, it.height),
                            result.list
                        )
                        binding.resultPanel.isVisible = true
                    }
                }
            }
            resultBackPressHandler.isEnabled = true
            if (com.lollipop.techo.BuildConfig.DEBUG) {
                result.list.forEach {
                    log("OnBarcodeScanResult", it.describe.displayValue + ", " + it.info + ", " + String(it.describe.bytes))
                    log("OnBarcodeScanResult", "cornerPoints = ", it.describe.cornerPoints.size)
                }
            }
        }
    }

    private fun resumeScan() {
        resultBackPressHandler.isEnabled = false
        binding.resultImageView.setImageDrawable(null)
        binding.resultPanel.isVisible = false
        qrReaderHelper.analyzerEnable = true
    }

    override fun onCodeSelected(code: BarcodeWrapper) {
        Toast.makeText(this, code.describe.displayValue, Toast.LENGTH_SHORT).show()
    }

}