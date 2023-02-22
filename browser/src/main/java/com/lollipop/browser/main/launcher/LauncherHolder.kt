package com.lollipop.browser.main.launcher

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.browser.databinding.ItemMainPageLauncherBinding
import com.lollipop.stitch.ColorStitchView
import java.io.File

class LauncherHolder(
    private val binding: ItemMainPageLauncherBinding,
    private val onClickCallback: (Int) -> Unit,
    private val onLongClickCallback: (Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            onClickCallback: (Int) -> Unit,
            onLongClickCallback: (Int) -> Unit,
        ): LauncherHolder {
            return LauncherHolder(
                parent.bind(false),
                onClickCallback,
                onLongClickCallback,
            )
        }
    }

    init {
        binding.root.onClick {
            onHolderClick()
        }
        binding.root.setOnLongClickListener {
            onHolderLongClick()
        }
    }

    private fun onHolderClick() {
        onClickCallback(adapterPosition)
    }

    private fun onHolderLongClick(): Boolean {
        onLongClickCallback(adapterPosition)
        return true
    }

    fun bind(info: LauncherInfo) {
        setBackground(binding.backgroundView, info.backgroundColor)
        binding.labelView.text = info.label
        loadOrClear(binding.backgroundView, info.backgroundFile)
        loadOrClear(binding.iconView, info.icon)
    }

    private fun loadOrClear(imageView: ImageView, file: File?) {
        if (file == null) {
            imageView.setImageDrawable(null)
            return
        }
        Glide.with(imageView).load(file).into(imageView)
    }

    private fun setBackground(view: ColorStitchView, colorList: List<Int>) {
        view.resetColor(colorList, updatePiece = false)
    }

}