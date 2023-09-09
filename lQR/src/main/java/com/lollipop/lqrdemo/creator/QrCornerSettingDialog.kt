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

    private var currentMode = option.mode

    override fun onCreateDialogView(): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.roundModeIcon.setImageDrawable(roundModeDrawable)
        binding.cutModeIcon.setImageDrawable(cutModeDrawable)
        binding.squircleModeIcon.setImageDrawable(squircleModeDrawable)
        binding.titleView.text = option.title
        updateMode(option.mode)
        binding.slider.value = if (option.weightEnable) {
            option.value * 100
        } else {
            option.value
        }
        binding.weightModeSwitch.isChecked = option.weightEnable
    }

    private fun updateMode(mode: Mode) {
        currentMode = mode
        binding.noneModeIcon.isSelected = mode == Mode.None
        binding.roundModeIcon.isSelected = mode == Mode.Round
        binding.cutModeIcon.isSelected = mode == Mode.Cut
        binding.squircleModeIcon.isSelected = mode == Mode.Squircle
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

        binding.weightModeSwitch.thumbTintList = ColorStateList.valueOf(fg)
        binding.weightModeSwitch.trackTintList = ColorStateList.valueOf(fg.changeAlpha(0.5F))
    }

    override fun dismiss() {
        super.dismiss()
        val weightEnable = binding.weightModeSwitch.isChecked
        var value = binding.slider.value
        if (weightEnable) {
            value *= 0.01F
        }
        option.callback.onResult(currentMode, weightEnable, value)
    }

    enum class Mode {
        Cut,
        Round,
        Squircle,
        None
    }

    class Option(
        val context: Context,
        val title: CharSequence,
        val mode: Mode,
        val weightEnable: Boolean,
        val value: Float,
        val callback: Callback
    )

    fun interface Callback {
        fun onResult(mode: Mode, weightEnable: Boolean, value: Float)
    }

}