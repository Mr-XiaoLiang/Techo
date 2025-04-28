package com.lollipop.lqrdemo.preview.renderer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.lollipop.base.util.Clipboard
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.databinding.BarcodePreviewWifiBinding
import com.lollipop.lqrdemo.preview.BarcodePreviewRenderer
import com.lollipop.pigment.Pigment
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper

class WifiViewRenderer : BarcodePreviewRenderer {

    private var binding: BarcodePreviewWifiBinding? = null

    override fun getView(container: ViewGroup): View {
        val oldBinding = binding
        if (oldBinding != null) {
            return oldBinding.root
        }
        val newBinding = BarcodePreviewWifiBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )
        binding = newBinding
        newBinding.ssidView.setOnClickListener(ClickToCopyDelegate())
        newBinding.passwordView.setOnClickListener(ClickToCopyDelegate())
        return newBinding.root
    }

    override fun render(barcode: BarcodeWrapper) {
        val info = barcode.info
        if (info !is BarcodeInfo.Wifi) {
            return
        }
        val ssid = info.ssid
        val password = info.password
        binding?.apply {
            ssidView.text = ssid
            passwordView.text = password
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        binding?.apply {
            root.setBackgroundColor(pigment.backgroundColor)
            ssidView.setTextColor(pigment.onBackgroundTitle)
            passwordView.setTextColor(pigment.onBackgroundBody)
        }
    }

    private class ClickToCopyDelegate : View.OnClickListener {


        override fun onClick(v: View?) {
            v ?: return
            if (v is TextView) {
                val context = v.context
                Clipboard.copy(context, value = v.text.toString())
                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            }
        }

    }

}