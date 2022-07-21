package com.lollipop.techo.list.detail

import android.view.ViewGroup
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.ItemTitleBinding
import com.lollipop.techo.util.RichTextHelper

/**
 * @author lollipop
 * @date 2021/11/13 22:09
 */
class TitleInfoHolder(
    view: EditItemView<ItemTitleBinding>
) : EditHolder<ItemTitleBinding>(view) {

    companion object {
        fun create(group: ViewGroup): TitleInfoHolder {
            return TitleInfoHolder(group.bindContent())
        }
    }

    override val canDrag: Boolean
        get() {
            return false
        }

    init {
        binding.content.textView.setHint(R.string.hint_title_input)
    }

    fun bind(info: TechoItem.Title) {
        update()
        updateContent(info)
    }

    private fun updateContent(info: TechoItem) {
        RichTextHelper.startRichFlow().addRichInfo(info).into(binding.content.textView)
    }

}