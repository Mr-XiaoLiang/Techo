package com.lollipop.lqrdemo.creator

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.WindowInsetsType
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.databinding.DialogInputBinding

class InputPopupWindow(context: Context, private val option: Option) : Dialog(context) {

    private val binding: DialogInputBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.let {
            WindowInsetsHelper.initWindowFlag(it)
            updateWindowAttributes(it)
        }
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.inputContent.fixInsetsByPadding(WindowInsetsEdge.CONTENT).apply {
            windowInsetsOperator.insetsType = WindowInsetsType.IME
        }
        binding.inputEditView.setText(option.preset)
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