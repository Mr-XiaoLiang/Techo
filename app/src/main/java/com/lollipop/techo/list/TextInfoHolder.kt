package com.lollipop.techo.list

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.lollipop.base.util.bind
import com.lollipop.techo.data.CheckBoxItem
import com.lollipop.techo.data.NumberItem
import com.lollipop.techo.data.TextItem
import com.lollipop.techo.databinding.ItemEditGroupBinding
import com.lollipop.techo.databinding.ItemTextBinding
import com.lollipop.techo.util.RichTextHelper

/**
 * @author lollipop
 * @date 2021/11/13 22:09
 */
class TextInfoHolder(
    private val binding: ItemTextBinding,
    optionBinding: ItemEditGroupBinding
) : EditHolder(optionBinding) {

    companion object {
        fun create(group: ViewGroup): TextInfoHolder {
            val optionBinding = createItemView(group)
            return TextInfoHolder(getContentGroup(optionBinding).bind(true), optionBinding)
        }
    }

    fun bind(info: TextItem) {
        binding.checkBox.isVisible = false
        binding.numberView.isVisible = false
        RichTextHelper.startRichFlow().addRichInfo(info).into(binding.textView)
    }

    fun bind(info: NumberItem) {
        binding.checkBox.isVisible = false
        binding.numberView.isVisible = true
        binding.numberView.text = info.number.toString()
        RichTextHelper.startRichFlow().addRichInfo(info).into(binding.textView)
    }

    fun bind(info: CheckBoxItem) {
        binding.checkBox.isVisible = true
        binding.numberView.isVisible = false
        binding.checkBox.isChecked = info.isChecked
        RichTextHelper.startRichFlow().addRichInfo(info).into(binding.textView)
    }

}