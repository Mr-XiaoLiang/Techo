package com.lollipop.techo.qr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.qr.BarcodeHelper
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ActivityQrCreateBinding

class QrCreateActivity : BaseActivity() {

    companion object {

        private const val QR_INFO = "QR_INFO"

        fun start(context: Context, info: String) {
            start<QrCreateActivity>(context) {
                it.putExtra(QR_INFO, info)
            }
        }
    }

    private val binding: ActivityQrCreateBinding by lazyBind()

    private val qrInfo: String
        get() {
            return intent.getStringExtra(QR_INFO) ?: ""
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backButton.onClick {
            notifyBackPress()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        updateBarcode()
    }

    private fun updateBarcode() {
        val info = qrInfo
        if (info.isEmpty()) {
            binding.qrImageView.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            doAsync {
                val result = BarcodeHelper.createWriter(this)
                    .encode(info)
                    .drawBitmap(null)
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