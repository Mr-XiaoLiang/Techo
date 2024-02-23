package com.lollipop.techo.activity

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.onClick
import com.lollipop.techo.data.TechoTheme
import com.lollipop.techo.databinding.ItemTechoThemeBinding

class TechoThemeSelectActivity : BasicListActivity() {

//    private val binding

    private class ItemHolder(
        private val binding: ItemTechoThemeBinding,
        private val onItemClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.themeCardView.onClick {
                onItemClick()
            }
            binding.deleteThemeButton.onClick {
                onDeleteClick()
            }
            binding.themeCardView.setOnLongClickListener {
                onItemLongClick()
                true
            }
            binding.optionPanelView.onClick {
                cancelOptionPanel()
            }
        }

        private fun onItemClick() {
            onItemClick(adapterPosition)
        }

        private fun onDeleteClick() {
            onDeleteClick(adapterPosition)
        }

        private fun onItemLongClick() {
            binding.optionPanelView.isVisible = true
        }

        private fun cancelOptionPanel() {
            binding.optionPanelView.isVisible = false
        }

        fun bind(theme: TechoTheme.Snapshot) {
            cancelOptionPanel()
            // TODO
        }

    }

}