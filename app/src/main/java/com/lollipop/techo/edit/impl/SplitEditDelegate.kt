package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.SplitItem
import com.lollipop.techo.data.SplitStyle
import com.lollipop.techo.databinding.ItemSplitBinding
import com.lollipop.techo.databinding.PanelSplitSelectBinding
import com.lollipop.techo.edit.EditDelegate

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class SplitEditDelegate : EditDelegate() {

    private var binding: PanelSplitSelectBinding? = null

    override fun isSupport(info: BaseTechoItem): Boolean {
        return info is SplitItem
    }

    override fun onCreateView(container: ViewGroup): View {
        val newBinding: PanelSplitSelectBinding = container.bind(false)
        binding = newBinding
        return newBinding.root
    }

    private class Holder(
        private val binding: ItemSplitBinding,
        private val onClickCallback: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(parent: ViewGroup, onClickCallback: (Int) -> Unit): Holder {
                return Holder(parent.bind(), onClickCallback)
            }
        }

        init {
            itemView.onClick {
                onItemClick()
            }
        }

        fun bind(type: SplitStyle) {

        }

        private fun onItemClick() {
            onClickCallback.invoke(adapterPosition)
        }

    }

}