package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lollipop.base.graphics.LDrawable
import com.lollipop.base.util.ActivityLauncherHelper
import com.lollipop.base.util.bind
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.pigment.Pigment
import com.lollipop.techo.R
import com.lollipop.techo.data.AppTheme
import com.lollipop.techo.data.TechoTheme
import com.lollipop.techo.databinding.ItemTechoThemeBinding
import java.io.File

class TechoThemeSelectActivity : BasicListActivity() {

    companion object {
        private const val PARAMS_SELECT_MODE = "SELECT_MODE"
        private const val RESULT_THEME_KEY = "THEME_KEY"

        val LAUNCHER: Class<out ActivityResultContract<Boolean?, String>> =
            ResultContract::class.java
    }

    private val dataList = ArrayList<ThemeInfo>()
    private val adapter = ThemeAdapter(dataList, ::onThemeClick, ::onThemeDelete)
    private val isSelectMode: Boolean by lazy {
        intent.getBooleanExtra(PARAMS_SELECT_MODE, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRecyclerView {
            it.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            it.adapter = adapter
        }
        adapter.onPigmentChanged(currentPigment)
        setResult(RESULT_CANCELED)
    }

    override fun onResume() {
        super.onResume()
        updateThemeList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateThemeList() {
        showLoading()
        doAsync {
            val srcList = TechoTheme.getCustomThemeList()
            val themeList = ArrayList<ThemeInfo>()
            srcList.forEach {
                themeList.add(
                    ThemeInfo(
                        it.getPigment(),
                        it.key,
                        if (it is TechoTheme.Custom) {
                            it.file
                        } else {
                            null
                        }
                    )
                )
            }
            onUI {
                hideLoading()
                dataList.clear()
                dataList.addAll(themeList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        adapter.onPigmentChanged(currentPigment)
    }

    private fun onThemeClick(position: Int, info: ThemeInfo) {
        if (isSelectMode) {
            setResult(RESULT_OK, Intent().apply {
                putExtra(RESULT_THEME_KEY, info.name)
            })
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun onThemeDelete(position: Int, info: ThemeInfo) {
        if (info.file == null) {
            Toast.makeText(
                this,
                R.string.toast_remove_custom_theme_refuse,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.dialog_msg_remove_custom_theme, info.name))
            .setPositiveButton(R.string.dialog_positive_remove_custom_theme) { dialog, _ ->
                dialog.dismiss()
                deleteTheme(position, info)
            }
            .setNegativeButton(R.string.dialog_negative_remove_custom_theme) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteTheme(position: Int, info: ThemeInfo) {
        if (position >= 0 && position < dataList.size) {
            dataList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
        TechoTheme.removeTheme(info.name)
        doAsync {
            info.file?.delete()
        }
    }

    private class ThemeAdapter(
        private val dataList: List<ThemeInfo>,
        private val onItemClick: (Int, ThemeInfo) -> Unit,
        private val onItemDelete: (Int, ThemeInfo) -> Unit
    ) : RecyclerView.Adapter<ItemHolder>() {


        private var pigment = AppTheme.current

        @SuppressLint("NotifyDataSetChanged")
        fun onPigmentChanged(p: Pigment) {
            pigment = p
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(parent.bind(false), ::onItemClick, ::onItemDelete)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val info = dataList[position]
            holder.bind(info.theme, info.name, pigment)
        }

        private fun onItemClick(position: Int) {
            if (position < 0 || position >= dataList.size) {
                return
            }
            onItemClick(position, dataList[position])
        }

        private fun onItemDelete(position: Int) {
            if (position < 0 || position >= dataList.size) {
                return
            }
            onItemDelete(position, dataList[position])
        }
    }

    private class ThemeInfo(
        val theme: TechoTheme.Snapshot,
        val name: String,
        val file: File?
    )

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

    class ResultContract : ActivityLauncherHelper.Simple<Boolean?, String>() {

        override val activityClass: Class<out Activity> = TechoDetailActivity::class.java

        override fun putParams(intent: Intent, input: Boolean?) {
            super.putParams(intent, input)
            intent.putExtra(PARAMS_SELECT_MODE, input ?: true)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String {
            intent ?: return ""
            if (resultCode != Activity.RESULT_OK) {
                return ""
            }
            return intent.getStringExtra(RESULT_THEME_KEY) ?: ""
        }

    }

}