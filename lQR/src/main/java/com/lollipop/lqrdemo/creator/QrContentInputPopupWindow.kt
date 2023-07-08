package com.lollipop.lqrdemo.creator

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.WindowInsetsType
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.requestBoard
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.databinding.DialogInputBinding
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.PigmentWallpaperCenter

class QrContentInputPopupWindow(context: Context, private val option: Option) : Dialog(context) {

    companion object {
        private const val WARNING_LENGTH = 200

        fun show(
            context: Context,
            preset: String,
            openBuildPage: () -> Unit,
            callback: (String) -> Unit
        ) {
            QrContentInputPopupWindow(context, Option(preset, openBuildPage, callback)).show()
        }

    }

    private val binding: DialogInputBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.TOP)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        binding.doneButton.onClick {
            option.callback(binding.inputEditView.text?.toString() ?: "")
            dismiss()
        }
        binding.builderButton.onClick {
            option.openBuildPage()
            dismiss()
        }

        PigmentWallpaperCenter.pigment?.let { pigment ->
            binding.inputContent.setBackgroundColor(pigment.extreme)

            BlendMode.flow(pigment.primaryColor)
                .blend(pigment.extreme, 0.7F) {
                    binding.inputEditView.setBackgroundColor(it)
                }.content {
                    binding.inputEditView.setTextColor(it)
                }

            binding.doneButtonIcon.imageTintList = ColorStateList.valueOf(pigment.onExtremeBody)
            binding.doneButtonText.setTextColor(pigment.onExtremeBody)

            binding.builderButtonIcon.imageTintList = ColorStateList.valueOf(pigment.onExtremeBody)
            binding.builderButtonText.setTextColor(pigment.onExtremeBody)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            binding.inputEditView.requestBoard()
        }
    }

    class Option(
        val preset: String,
        val openBuildPage: () -> Unit,
        val callback: (String) -> Unit
    )

}