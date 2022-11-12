package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.log
import com.lollipop.qr.BarcodeHelper
import com.lollipop.techo.databinding.ActivityQrScanningBinding

class QrScanningActivity : AppCompatActivity() {

    private val binding: ActivityQrScanningBinding by lazyBind()

    private val qrReaderHelper = BarcodeHelper.createCameraReader(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        qrReaderHelper.bindContainer(binding.previewContainer)
        qrReaderHelper.addOnBarcodeScanResultListener {
            log("OnBarcodeScanResult: ${it.list.size}")
            if (!it.isEmpty) {
                qrReaderHelper.analyzerEnable = false
                it.list.forEach { result ->
                    log(result.describe.displayValue + ", " + result.info::class.java)
                }
            }
        }
    }

}