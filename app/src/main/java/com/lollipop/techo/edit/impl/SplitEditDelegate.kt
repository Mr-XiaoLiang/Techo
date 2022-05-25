package com.lollipop.techo.edit.impl

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.SplitItem
import com.lollipop.techo.databinding.ItemSplitBinding
import com.lollipop.techo.databinding.PanelSplitSelectBinding
import com.lollipop.techo.edit.EditDelegate
import com.lollipop.techo.split.SplitLoader

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class SplitEditDelegate : EditDelegate() {

    private var binding: PanelSplitSelectBinding? = null

    private val dataList = ArrayList<SplitItem>()

    private val adapter = Adapter(dataList, ::onSplitClick)

    override fun isSupport(info: BaseTechoItem): Boolean {
        return info is SplitItem
    }

    override fun onCreateView(container: ViewGroup): View {
        val newBinding: PanelSplitSelectBinding = container.bind(false)
        binding = newBinding
        return newBinding.root
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        binding?.splitSelectGroup?.let { rv ->
            rv.layoutManager = LinearLayoutManager(rv.context, RecyclerView.VERTICAL, false)
            rv.adapter = adapter
        }
        loadConfig()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadConfig() {
        doAsync {
            val c = context
            if (c != null) {
                val list = SplitLoader.read(c)
                onUI {
                    dataList.clear()
                    dataList.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun onSplitClick(item: SplitItem) {
        // TODO
    }

    private class Adapter(
        private val data: List<SplitItem>,
        private val onSelectedSplit: (SplitItem) -> Unit
    ) : RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder.create(parent, ::onItemClick)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        private fun onItemClick(position: Int) {
            if (position in data.indices) {
                onSelectedSplit(data[position])
            }
        }

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

        fun bind(info: SplitItem) {
            binding.splitView.load(info.style, info.flagValue)
        }

        private fun onItemClick() {
            onClickCallback.invoke(adapterPosition)
        }

    }

}