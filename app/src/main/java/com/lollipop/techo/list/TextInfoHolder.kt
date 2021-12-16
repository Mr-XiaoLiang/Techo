package com.lollipop.techo.list

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.lollipop.techo.R
import com.lollipop.techo.data.BaseTextItem
import com.lollipop.techo.data.CheckBoxItem
import com.lollipop.techo.data.NumberItem
import com.lollipop.techo.data.TextItem
import com.lollipop.techo.databinding.ItemTextBinding
import com.lollipop.techo.util.RichTextHelper

/**
 * @author lollipop
 * @date 2021/11/13 22:09
 */
class TextInfoHolder(
    view: EditItemView<ItemTextBinding>
) : EditHolder<ItemTextBinding>(view) {

    companion object {
        fun create(group: ViewGroup): TextInfoHolder {
            return TextInfoHolder(group.bindContent())
        }
    }

    init {
        binding.content.textView.setHint(R.string.hint_text_input)
    }

    fun bind(info: TextItem) {
        changedStyle(checkBox = false, number = false)
        updateContent(info)
    }

    fun bind(info: NumberItem) {
        changedStyle(checkBox = false, number = true)
        binding.content.numberView.text = info.number.toString()
        updateContent(info)
    }

    fun bind(info: CheckBoxItem) {
        changedStyle(checkBox = true, number = false)
        binding.content.checkBox.isChecked = info.isChecked
        updateContent(info)
    }

    private fun updateContent(info: BaseTextItem) {
        RichTextHelper.startRichFlow().addRichInfo(info).into(binding.content.textView)
    }

    private fun changedStyle(checkBox: Boolean, number: Boolean) {
        binding.content.checkBox.isVisible = checkBox
        binding.content.numberView.isVisible = number
    }

}