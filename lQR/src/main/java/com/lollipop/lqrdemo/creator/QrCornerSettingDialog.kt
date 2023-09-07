package com.lollipop.lqrdemo.creator

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
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
        // 这些都是啥呀。。。。
        binding.slider.tickInactiveTintList
        binding.slider.trackInactiveTintList
        binding.slider.haloTintList
        binding.slider.thumbTintList
        binding.slider.tickActiveTintList
        binding.slider.trackActiveTintList
    }

    class Option(
        val context: Context,
        val title: CharSequence
    )

}