package com.lollipop.techo.list

import android.view.ViewGroup
import com.lollipop.base.util.bind
import com.lollipop.techo.databinding.ItemEditGroupBinding
import com.lollipop.techo.databinding.ItemTextBinding

/**
 * @author lollipop
 * @date 2021/11/13 22:09
 */
class TextInfoHolder(
    private val binding: ItemTextBinding,
    optionBinding: ItemEditGroupBinding
): EditHolder(optionBinding) {

    companion object {
        fun create(group: ViewGroup): TextInfoHolder {
            val optionBinding = createItemView(group)
            return TextInfoHolder(getContentGroup(optionBinding).bind(true), optionBinding)
        }
    }

}