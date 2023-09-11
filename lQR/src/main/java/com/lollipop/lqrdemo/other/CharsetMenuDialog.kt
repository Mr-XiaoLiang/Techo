package com.lollipop.lqrdemo.other

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.base.BaseBottomDialog
import com.lollipop.lqrdemo.databinding.DialogCharsetMenuBinding
import com.lollipop.lqrdemo.databinding.ItemDialogCharsetMenuBinding
import java.nio.charset.Charset

class CharsetMenuDialog(
    context: Context,
    private val onClick: (Charset) -> Unit
) : BaseBottomDialog(context) {

    private val binding: DialogCharsetMenuBinding by lazyBind()

    override val contentView: View
        get() = binding.root

    private val list = ArrayList<Item>()

    private val adapter = Adapter(list, ::onItemClick)

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        list.clear()
        Charset.availableCharsets().forEach { entry ->
            list.add(Item(entry.key, entry.value))
        }
        adapter.notifyDataSetChanged()
    }

    private fun onItemClick(item: Item) {
        onClick(item.charset)
        dismiss()
    }

    private class Adapter(
        private val list: List<Item>,
        private val onClick: (Item) -> Unit
    ) : RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(parent.bind(), ::onItemClick)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        private fun onItemClick(position: Int) {
            if (position < 0 || position >= list.size) {
                return
            }
            onClick(list[position])
        }

    }

    private class Holder(
        private val binding: ItemDialogCharsetMenuBinding,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.onClick {
                onItemClick()
            }
        }

        private fun onItemClick() {
            onClick(adapterPosition)
        }

        fun bind(item: Item) {
            binding.textView.text = item.name
        }

    }

    private class Item(
        val name: String,
        val charset: Charset
    )

}