package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.qr.QRReaderHelper
import com.lollipop.techo.databinding.ActivityQrScanningBinding

class QrScanningActivity : AppCompatActivity() {

    private val binding: ActivityQrScanningBinding by lazyBind()

    private val qrReaderHelper = QRReaderHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        qrReaderHelper.bindContainer(binding.previewContainer)
    }


    override fun onDestroy() {
        super.onDestroy()
        qrReaderHelper.onDestroy()
    }
}