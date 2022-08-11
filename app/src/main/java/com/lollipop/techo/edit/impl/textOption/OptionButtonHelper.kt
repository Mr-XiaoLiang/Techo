package com.lollipop.techo.edit.impl.textOption

import android.view.View
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TextSpan

internal class OptionButtonHelper(
    private val button: View,
    private val style: FontStyle,
    private val onOptionChanged: (style: FontStyle, has: Boolean) -> Unit
) : View.OnClickListener {

    companion object {
        fun bind(
            btn: View,
            style: FontStyle,
            onOptionChanged: (style: FontStyle, has: Boolean) -> Unit
        ): OptionButtonHelper {
            return OptionButtonHelper(btn, style, onOptionChanged)
        }
    }

    init {
        button.setOnClickListener(this)
    }

    fun check(textSpan: TextSpan) {
        button.isSelected = textSpan.hasStyle(style)
    }

    override fun onClick(v: View?) {
        val selected = !button.isSelected
        button.isSelected = selected
        onOptionChanged(style, selected)
    }

}