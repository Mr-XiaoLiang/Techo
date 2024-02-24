package com.lollipop.techo.activity

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.graphics.LDrawable
import com.lollipop.base.util.onClick
import com.lollipop.pigment.Pigment
import com.lollipop.techo.data.TechoTheme
import com.lollipop.techo.databinding.ItemTechoThemeBinding

class TechoThemeSelectActivity : BasicListActivity() {

//    private val binding


    private class ThemeAdapter() {
        // TODO 数据结构怎么定？
    }

    private class ItemHolder(
        private val binding: ItemTechoThemeBinding,
        private val onItemClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /*
        {
            "key": "",
            "base": "light/dark",
            "primaryColor": "#FFFFFFFF",
            "secondaryColor": "#FFFFFFFF",
            "primaryVariant": "#FFFFFFFF",
            "onPrimaryTitle": "#FFFFFFFF",
            "onPrimaryBody": "#FFFFFFFF",
            "secondaryVariant": "#FFFFFFFF",
            "onSecondaryTitle": "#FFFFFFFF",
            "onSecondaryBody": "#FFFFFFFF",
            "backgroundColor": "#FFFFFFFF",
            "onBackgroundTitle": "#FFFFFFFF",
            "onBackgroundBody": "#FFFFFFFF",
            "extreme": "#FFFFFFFF",
            "extremeReversal": "#FFFFFFFF",
            "onExtremeTitle": "#FFFFFFFF",
            "onExtremeBody": "#FFFFFFFF"
        }
        */

        private val primaryBackground = LinearGradientDrawable()
        private val secondaryBackground = LinearGradientDrawable()

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
            binding.primaryCardView.background = primaryBackground
            binding.secondaryCardView.background = secondaryBackground
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

        fun bind(theme: TechoTheme.Snapshot, name: String, pigment: Pigment) {
            cancelOptionPanel()

            primaryBackground.resetColor(theme.primaryColor, theme.primaryVariant)
            secondaryBackground.resetColor(theme.secondaryColor, theme.secondaryVariant)
            binding.backgroundCardView.setBackgroundColor(theme.backgroundColor)
            binding.extremeCardView.setBackgroundColor(theme.extreme)

            binding.onPrimaryTitleView.setTextColor(theme.onPrimaryTitle)
            binding.onPrimaryBodyView.setTextColor(theme.onPrimaryBody)

            binding.onSecondaryTitleView.setTextColor(theme.onSecondaryTitle)
            binding.onSecondaryBodyView.setTextColor(theme.onSecondaryBody)

            binding.onBackgroundTitleView.setTextColor(theme.onBackgroundTitle)
            binding.onBackgroundBodyView.setTextColor(theme.onBackgroundBody)

            binding.onExtremeTitleView.setTextColor(theme.onExtremeTitle)
            binding.onExtremeBodyView.setTextColor(theme.onExtremeBody)

            binding.themeName.text = name
            binding.themeName.setTextColor(pigment.onBackgroundBody)

        }

    }

    private class LinearGradientDrawable : LDrawable() {

        private var colorArray = IntArray(0)

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
        }

        fun resetColor(vararg colors: Int) {
            colorArray = colors
            buildGradient()
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            buildGradient()
        }

        private fun buildGradient() {
            if (colorArray.isEmpty()) {
                paint.color = Color.WHITE
                invalidateSelf()
                return
            }
            paint.color = colorArray[0]
            if (colorArray.size > 1) {
                val width = bounds.width()
                val height = bounds.height()
                if (width < 1 || height < 1) {
                    paint.shader = null
                } else {
                    val newShader = LinearGradient(
                        bounds.left.toFloat(),
                        bounds.top.toFloat(),
                        bounds.right.toFloat(),
                        bounds.bottom.toFloat(),
                        colorArray,
                        null,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = newShader
                }
            } else {
                paint.shader = null
            }
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRect(bounds, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

    }

}