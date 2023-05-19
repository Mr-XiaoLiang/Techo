package com.lollipop.lqrdemo.creator

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.WindowInsetsType
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.databinding.DialogInputBinding

class QrContentInputPopupWindow(context: Context, private val option: Option) : Dialog(context) {

    companion object {
        private const val WARNING_LENGTH = 200

        fun show(context: Context, preset: CharSequence, callback: (CharSequence) -> Unit) {
            QrContentInputPopupWindow(context, Option(preset, callback)).show()
        }

    }

    private val binding: DialogInputBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.let {
            WindowInsetsHelper.initWindowFlag(it)
            updateWindowAttributes(it)
        }
        binding.inputContent.fixInsetsByPadding(WindowInsetsEdge.CONTENT).apply {
            windowInsetsOperator.insetsType = WindowInsetsType.IME
        }
        binding.inputEditView.setText(option.preset)
        binding.inputEditView.doAfterTextChanged { text ->
            binding.warnTextView.text = context.getString(
                R.string.warning_qr_content_length,
                WARNING_LENGTH,
                text?.length ?: 0
            )
        }
    }

    private fun updateWindowAttributes(window: Window) {
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        window.attributes = window.attributes.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    override fun dismiss() {
        option.callback(binding.inputEditView.text ?: "")
        super.dismiss()
    }

    class Option(
        val preset: CharSequence,
        val callback: (CharSequence) -> Unit
    )

}