package com.lollipop.techo.qr

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.*
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.BarcodeHelper
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ActivityQrCreateBinding

class QrCreateActivity : AppCompatActivity() {

    companion object {

        private const val BARCODE_INFO = "BARCODE_INFO"
        private const val BARCODE_FORMAT = "BARCODE_FORMAT"

        fun start(context: Context, info: String, format: BarcodeFormat = BarcodeFormat.QR_CODE) {
            context.startActivity(Intent(context, QrCreateActivity::class.java).apply {
                putExtra(BARCODE_INFO, info)
                putExtra(BARCODE_FORMAT, format.code)
            })
        }
    }

    private val binding: ActivityQrCreateBinding by lazyBind()

    private val codeInfo: String
        get() {
            return intent.getStringExtra(BARCODE_INFO) ?: ""
        }

    private val codeFormat: BarcodeFormat
        get() {
            val code = intent.getIntExtra(BARCODE_FORMAT, BarcodeFormat.QR_CODE.code)
            return BarcodeFormat.entries.find { it.code == code } ?: BarcodeFormat.QR_CODE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backButton.onClick {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.fixInsetsByMargin(WindowInsetsEdge.HEADER)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        binding.qrImageView.post {
            updateBarcode()
        }
    }

    private fun updateBarcode() {
        val info = codeInfo
        if (info.isEmpty()) {
            binding.qrImageView.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val builder = BarcodeHelper.createWriter(this)
                .encode(info)
                .size(binding.qrImageView.width, binding.qrImageView.height)
                .color(Color.BLACK, Color.TRANSPARENT)
                .format(codeFormat)
            doAsync {
                val result = builder.drawBitmap(null)
                onUI {
                    val bitmap = result.getOrNull()
                    if (bitmap != null) {
                        binding.qrImageView.setImageBitmap(bitmap)
                    } else {
                        binding.qrImageView.setImageResource(R.mipmap.ic_launcher_round)
                    }
                }
            }
        }
    }

}