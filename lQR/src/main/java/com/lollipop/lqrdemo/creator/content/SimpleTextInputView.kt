package com.lollipop.lqrdemo.creator.content

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

class SimpleTextInputView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null,
) : TextInputEditText(context, attributeSet) {

    private var textChangedListener: ((CharSequence) -> Unit)? = null

    private val textWatch = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            onTextChanged(s ?: "")
        }

    }

    init {
        addTextChangedListener(textWatch)
    }

    private fun onTextChanged(value: CharSequence) {
        this.textChangedListener?.invoke(value)
    }

    fun onTextChanged(listener: ((CharSequence) -> Unit)?) {
        this.textChangedListener = listener
    }


}