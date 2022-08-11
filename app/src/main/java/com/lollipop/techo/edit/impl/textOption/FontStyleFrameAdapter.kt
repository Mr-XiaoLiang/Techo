package com.lollipop.techo.edit.impl.textOption

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.techo.data.TextSpan
import com.lollipop.techo.databinding.ItemPanelTextOptionFrameAddBinding
import com.lollipop.techo.databinding.ItemPanelTextOptionFrameBinding

internal class FontStyleFrameAdapter(
    private val list: List<TextSpan>,
    private val textProvider: (TextSpan) -> CharSequence,
    private val isSelected: (TextSpan) -> Boolean,
    private val onAddButtonClick: () -> Unit,
    private val onFrameClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val STYLE_ADD = 0
        private const val STYLE_FRAME = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            STYLE_ADD -> {
                AddFontStyleFrameHolder(parent.bind(), onAddButtonClick)
            }
            else -> {
                FontStyleFrameHolder(parent.bind(), onFrameClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FontStyleFrameHolder) {
            val textSpan = list[position]
            holder.bind(textProvider(textSpan), isSelected(textSpan))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= list.size) {
            return STYLE_ADD
        }
        return STYLE_FRAME
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    private class FontStyleFrameHolder(
        private val binding: ItemPanelTextOptionFrameBinding,
        private val onItemClickListener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClickListener(adapterPosition) }
        }

        fun bind(text: CharSequence, isSelected: Boolean) {
            binding.labelView.text = text
            binding.selectedIcon.isVisible = isSelected
        }
    }

    private class AddFontStyleFrameHolder(
        binding: ItemPanelTextOptionFrameAddBinding,
        private val onItemClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener { onItemClickListener() }
        }
    }

}