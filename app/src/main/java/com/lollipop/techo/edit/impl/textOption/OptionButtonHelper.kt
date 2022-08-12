package com.lollipop.techo.edit.impl.textOption

import android.view.View
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TextSpan

internal class OptionButtonHelper(
    private val onOptionChanged: (style: FontStyle, has: Boolean) -> Unit
) {

    private val fontStyleHolderList = ArrayList<Holder>(FontStyle.values().size)

    fun bind(vararg pairArray: Pair<View, FontStyle>) {
        pairArray.forEach {
            fontStyleHolderList.add(Holder.bind(it.first, it.second, onOptionChanged))
        }
    }

    fun check(textSpan: TextSpan) {
        fontStyleHolderList.forEach {
            it.check(textSpan)
        }
    }

    class Holder(
        private val button: View,
        private val style: FontStyle,
        private val onOptionChanged: (style: FontStyle, has: Boolean) -> Unit
    ) : View.OnClickListener {
        companion object {
            fun bind(
                btn: View,
                style: FontStyle,
                onOptionChanged: (style: FontStyle, has: Boolean) -> Unit
            ): Holder {
                return Holder(btn, style, onOptionChanged)
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

}