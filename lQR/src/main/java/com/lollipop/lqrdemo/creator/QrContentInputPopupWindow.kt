package com.lollipop.lqrdemo.creator

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.requestBoard
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.WindowInsetsType
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.PigmentTheme
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
            windowInsetsOperator.insetsType = WindowInsetsType.Ime
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
            PigmentTheme.getForePanelBackground(pigment) { bg, btn ->
                binding.inputContent.setBackgroundColor(bg)

                binding.doneButtonIcon.imageTintList = ColorStateList.valueOf(btn)
                binding.doneButtonText.setTextColor(btn)

                binding.builderButtonIcon.imageTintList = ColorStateList.valueOf(btn)
                binding.builderButtonText.setTextColor(btn)

                binding.warnTextView.setTextColor(BlendMode.blend(btn, pigment.secondaryColor))
            }

            PigmentTheme.getForePanelInputBar(pigment) { bg, text ->
                binding.inputEditView.setBackgroundColor(bg)
                binding.inputEditView.setTextColor(text)
                binding.inputEditView.setHintTextColor(text)
            }

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