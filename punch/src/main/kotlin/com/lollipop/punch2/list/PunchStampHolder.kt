package com.lollipop.punch2.list

import android.content.res.ColorStateList
import androidx.compose.ui.graphics.toArgb
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.punch2.data.FlagInfo
import com.lollipop.punch2.databinding.ItemStampBinding
import com.lollipop.punch2.utils.ThemeHelper
import com.lollipop.punch2.utils.dp2px
import com.lollipop.punch2.utils.onClick
import com.lollipop.punch2.view.StampBackgroundDrawable

class PunchStampHolder(
    private val binding: ItemStampBinding,
    private val onPunchClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val stampBackgroundDrawable = StampBackgroundDrawable()

    init {
        binding.flagNameView.onClick {
            expandPunchButton()
        }
        binding.punchButton.onClick {
            onPunchButtonClick()
        }
        binding.flagNameView.background = stampBackgroundDrawable
        stampBackgroundDrawable.rectHeight = 50.dp2px
        stampBackgroundDrawable.rotation = 50F
    }

    private fun expandPunchButton() {
        binding.scrollView.scrollTo(binding.stampGroup.findOffset(binding.punchButton), 0)
    }

    private fun onPunchButtonClick() {
        onPunchClick(adapterPosition)
    }

    fun bind(info: FlagInfo) {
        binding.scrollView.scrollTo(binding.stampGroup.findOffset(binding.flagNameView), 0)

        binding.flagNameView.text = info.name
        stampBackgroundDrawable.color = info.color

        val colorBlend = ThemeHelper.colorBlend
        val itemBackground = colorBlend.extreme
        val primary = ThemeHelper.currentTheme?.primary?.toArgb() ?: itemBackground
        stampBackgroundDrawable.backgroundColor = itemBackground
        binding.flagNameView.setTextColor(colorBlend.title(info.color))
        binding.punchButton.setBackgroundColor(colorBlend.original(primary))
        binding.punchButton.imageTintList = ColorStateList.valueOf(colorBlend.body(primary))
    }

}