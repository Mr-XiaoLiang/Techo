package com.lollipop.techo.list.detail

import android.view.ViewGroup
import com.lollipop.techo.data.SplitItem
import com.lollipop.techo.databinding.ItemSplitBinding

/**
 * @author lollipop
 * @date 2021/11/20 12:53
 */
class SplitInfoHolder(
    view: EditItemView<ItemSplitBinding>
) : EditHolder<ItemSplitBinding>(view) {

    companion object {
        fun create(group: ViewGroup): SplitInfoHolder {
            return SplitInfoHolder(group.bindContent())
        }
    }

    fun bind(info: SplitItem) {
        binding.content.splitView.load(info.style, info.flagValue)
    }

}