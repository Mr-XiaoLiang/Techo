package com.lollipop.browser.secret

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.widget.addTextChangedListener
import com.lollipop.base.util.bind
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.browser.R
import com.lollipop.browser.base.BaseBottomSheetDialog
import com.lollipop.browser.databinding.DialogSecretBinding
import com.lollipop.browser.databinding.DialogSecretButtonBinding
import com.lollipop.browser.secret.SecretKeyboardView.Key.*

class SecretDialog(
    context: Context,
    private val commitCallback: OnSecretCommitCallback
) : BaseBottomSheetDialog(context),
    SecretKeyboardView.OnKeyButtonClickListener {

    companion object {
        fun show(context: Context, callback: OnSecretCommitCallback): SecretDialog {
            val dialog = SecretDialog(context, callback)
            dialog.show()
            return dialog
        }
    }

    private val binding: DialogSecretBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.secretKeyboardView.setProvider(KeyboardButtonProvider())
        binding.secretKeyboardView.addOnKeyButtonClickListener(this)
        binding.clearButton.onClick { clearInputValue() }
        binding.secretInputView.addTextChangedListener { onInputChanged(it) }
        clearInputValue()
    }

    private fun clearInputValue() {
        binding.secretInputView.text = ""
    }

    private fun onInputChanged(value: Editable?) {
        val emptyValue = value == null || value.isEmpty()
        binding.clearButton.isInvisible = emptyValue

    }

    override fun onKeyButtonClick(key: SecretKeyboardView.Key) {
        when (key) {
            NUMBER_0,
            NUMBER_1,
            NUMBER_2,
            NUMBER_3,
            NUMBER_4,
            NUMBER_5,
            NUMBER_6,
            NUMBER_7,
            NUMBER_8,
            NUMBER_9 -> {
                binding.secretInputView.append(key.numberString)
            }
            SUBMIT -> {
                onSubmitButtonClick()
            }
        }
    }

    private fun onSubmitButtonClick() {
        val text = binding.secretInputView.text
        if (text.isEmpty()) {
            return
        }
        commitCallback.onSecretCommit(text)
        dismiss()
    }

    fun interface OnSecretCommitCallback {
        fun onSecretCommit(value: CharSequence)
    }

    private class KeyboardButtonProvider : SecretKeyboardView.KeyButtonProvider {

        override fun createButton(parent: SecretKeyboardView, key: SecretKeyboardView.Key): View {
            val binding: DialogSecretButtonBinding = parent.bind(false)
            binding.keyButton.text = getText(parent.context, key)
            binding.keyButton.setBackgroundColor(getButtonColor(parent.context, key))
            return binding.root
        }

        private fun getButtonColor(context: Context, key: SecretKeyboardView.Key): Int {
            val colorId = when (key) {
                NUMBER_0 -> R.color.gray_2
                NUMBER_1,
                NUMBER_2,
                NUMBER_3 -> R.color.gray_3
                NUMBER_4,
                NUMBER_5,
                NUMBER_6 -> R.color.gray_4
                NUMBER_7,
                NUMBER_8,
                NUMBER_9 -> R.color.gray_5
                SUBMIT -> R.color.brand_3
            }
            return ContextCompat.getColor(context, colorId)
        }

        private fun getText(context: Context, key: SecretKeyboardView.Key): CharSequence {
            return when (key) {
                NUMBER_0,
                NUMBER_1,
                NUMBER_2,
                NUMBER_3,
                NUMBER_4,
                NUMBER_5,
                NUMBER_6,
                NUMBER_7,
                NUMBER_8,
                NUMBER_9 -> {
                    key.numberString
                }
                SUBMIT -> {
                    context.getString(R.string.done)
                }
            }
        }
    }

}