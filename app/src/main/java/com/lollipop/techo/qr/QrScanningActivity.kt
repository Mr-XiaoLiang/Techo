package com.lollipop.techo.qr

import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.core.view.isVisible
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsEdgeStrategy
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.comm.BarcodeResult
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.qr.view.CodeSelectionView
import com.lollipop.qr.view.FocusAnimationHelper
import com.lollipop.techo.activity.BaseActivity
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

    private val log by lazyLogD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
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

        binding.backButton.onClick {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.resultImageView.addOnCodeSelectedListener(this)

        binding.appBar.fixInsetsByMargin(WindowInsetsEdge.HEADER)
        binding.flashBtn.fixInsetsByMargin(WindowInsetsEdge.build {
            bottom = WindowInsetsEdgeStrategy.ACCUMULATE
        })
        binding.galleryBtn.fixInsetsByMargin(WindowInsetsEdge.build {
            bottom = WindowInsetsEdgeStrategy.ACCUMULATE
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
//            if (BuildConfig.DEBUG) {
//                result.list.forEach {
//                    log(
//                        "OnBarcodeScanResult" + it.describe.displayValue + ", " + it.info + ", " + String(
//                            it.describe.bytes
//                        )
//                    )
//                    log("OnBarcodeScanResult cornerPoints = " + it.describe.cornerPoints.size)
//                }
//            }
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