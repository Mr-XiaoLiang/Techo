package com.lollipop.techo.list

import android.view.ViewGroup
import com.lollipop.base.util.bind
import com.lollipop.techo.data.SplitItem
import com.lollipop.techo.databinding.ItemEditGroupBinding
import com.lollipop.techo.databinding.ItemSplitBinding

/**
 * @author lollipop
 * @date 2021/11/20 12:53
 */
class SplitInfoHolder(
    private val binding: ItemSplitBinding,
    optionBinding: ItemEditGroupBinding
) : EditHolder(optionBinding) {

    companion object {
        fun create(group: ViewGroup): SplitInfoHolder {
            val optionBinding = createItemView(group)
            return SplitInfoHolder(getContentGroup(optionBinding).bind(true), optionBinding)
        }
    }

    fun bind(info: SplitItem) {

    }

}