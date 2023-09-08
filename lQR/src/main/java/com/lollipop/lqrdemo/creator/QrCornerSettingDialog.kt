package com.lollipop.lqrdemo.creator

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.graphics.alpha
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.BaseCenterDialog
import com.lollipop.lqrdemo.databinding.DialogQrCornerSettingBinding
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentTint

class QrCornerSettingDialog(private val option: Option) : BaseCenterDialog(option.context) {

    private val binding: DialogQrCornerSettingBinding by lazyBind()

    private val cutModeDrawable by lazy {
        QrCornerSettingHelper.cutModeDrawable()
    }

    private val roundModeDrawable by lazy {
        QrCornerSettingHelper.roundModeDrawable()
    }

    private val squircleModeDrawable by lazy {
        QrCornerSettingHelper.squircleModeDrawable()
    }

    override fun onCreateDialogView(): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.roundModeIcon.setImageDrawable(roundModeDrawable)
        binding.cutModeIcon.setImageDrawable(cutModeDrawable)
        binding.squircleModeIcon.setImageDrawable(squircleModeDrawable)
        binding.titleView.text = option.title
    }

    private fun getModeBtnTintList(fg: Int): ColorStateList {
        return PigmentTint.getSelectStateList(fg, fg.changeAlpha(0.5f))
    }

    override fun onThemeChanged(pigment: Pigment?, fg: Int, bg: Int) {
        super.onThemeChanged(pigment, fg, bg)
        binding.contentGroup.setBackgroundColor(bg)
        val btnTintList = getModeBtnTintList(fg)
        binding.noneModeIcon.imageTintList = btnTintList
        binding.roundModeIcon.imageTintList = btnTintList
        binding.cutModeIcon.imageTintList = btnTintList
        binding.squircleModeIcon.imageTintList = btnTintList
        binding.titleView.setTextColor(fg)

//        binding.slider.tickInactiveTintList = ColorStateList.valueOf(fg.changeAlpha(0.5F))
//        binding.slider.tickActiveTintList = ColorStateList.valueOf(fg)

        binding.slider.trackActiveTintList = ColorStateList.valueOf(fg.changeAlpha(0.5F))
        binding.slider.trackInactiveTintList = ColorStateList.valueOf(fg)

        binding.slider.haloTintList = ColorStateList.valueOf(fg.changeAlpha(0.24F))
        binding.slider.thumbTintList = ColorStateList.valueOf(fg)
    }

    class Option(
        val context: Context,
        val title: CharSequence
    )

}