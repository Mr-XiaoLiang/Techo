package com.lollipop.lqrdemo

import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.lqrdemo.databinding.ActivityMainBinding
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.comm.BarcodeResult
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.qr.view.CodeSelectionView
import com.lollipop.qr.view.FocusAnimationHelper

class MainActivity : AppCompatActivity(), CodeSelectionView.OnCodeSelectedListener {

    private val binding: ActivityMainBinding by lazyBind()

    private val qrReaderHelper = BarcodeHelper.createCameraReader(this)

    private val focusAnimationHelper = FocusAnimationHelper {
        binding.focusView
    }

    private val resultBackPressHandler = BackPressHandler(false) {
        resumeScan()
    }

    private val log by lazyLogD()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)
        // 需要按照是否夜间模式来处理，等到做完夜间模式的适配，需要调整
        WindowInsetsHelper.getController(this).isAppearanceLightStatusBars = true
        setContentView(binding.root)
        resultBackPressHandler.bindTo(this)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
        initCamera()
        initView()
    }

    private fun initCamera() {
        qrReaderHelper.bindContainer(binding.previewContainer)
        qrReaderHelper.addOnFocusChangedListener(focusAnimationHelper)
        qrReaderHelper.addOnBarcodeScanResultListener(::onResult)
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

        binding.resultImageView.addOnCodeSelectedListener(this)

        binding.backButton.isVisible = false
        binding.backButton.onClick {
            onBackPressedDispatcher.onBackPressed()
        }
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
                        binding.resultImageView.isVisible = true
                        binding.backButton.isVisible = true
                        if (result.list.size == 1) {
                            // 只有一个的时候默认选中
                            onCodeSelected(result.list[0])
                        }
                    }
                }
            }
            resultBackPressHandler.isEnabled = true
            if (BuildConfig.DEBUG) {
                result.list.forEach {
                    log(
                        "OnBarcodeScanResult" + it.describe.displayValue + ", " + it.info + ", " + String(
                            it.describe.bytes
                        )
                    )
                    log("OnBarcodeScanResult cornerPoints = " + it.describe.cornerPoints.size)
                }
            }
        }
    }

    private fun resumeScan() {
        resultBackPressHandler.isEnabled = false
        binding.resultImageView.setImageDrawable(null)
        binding.resultImageView.isVisible = false
        qrReaderHelper.analyzerEnable = true
        binding.backButton.isVisible = false
    }

    override fun onCodeSelected(code: BarcodeWrapper) {
        BarcodeDetailDialog.show(this, code)
    }

}