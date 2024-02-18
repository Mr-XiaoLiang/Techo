package com.lollipop.techo.list.detail

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TechoTheme
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
        binding.content.textView.setOnClickListener {
            callOnEditClick()
        }
    }

    fun bind(info: TechoItem.Text) {
        update()
        changedStyle(checkBox = false, number = false)
        updateContent(info)
    }

    fun bind(info: TechoItem.Number) {
        update()
        changedStyle(checkBox = false, number = true)
        binding.content.numberView.text = info.number.toString()
        updateContent(info)
    }

    fun bind(info: TechoItem.CheckBox) {
        update()
        changedStyle(checkBox = true, number = false)
        binding.content.checkBox.isChecked = info.isChecked
        updateContent(info)
    }

    override fun updateDecoration(snapshot: TechoTheme.Snapshot) {
        super.updateDecoration(snapshot)
        binding.content.checkBox.tint(ColorStateList.valueOf(snapshot.onBackgroundTitle))
        binding.content.numberView.setTextColor(snapshot.onBackgroundTitle)
        binding.content.textView.setTextColor(snapshot.onBackgroundTitle)
    }

    private fun updateContent(info: TechoItem) {
        RichTextHelper.startRichFlow().addRichInfo(info).into(binding.content.textView)
    }

    private fun changedStyle(checkBox: Boolean, number: Boolean) {
        binding.content.checkBox.isVisible = checkBox
        binding.content.numberView.isVisible = number
    }

}