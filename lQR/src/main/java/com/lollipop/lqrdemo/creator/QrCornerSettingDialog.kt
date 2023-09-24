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

    override fun onCreateDialogView(): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.titleView.text = option.title
        binding.slider.value = if (option.weightEnable) {
            option.value * 100
        } else {
            option.value
        }
        binding.weightModeSwitch.isChecked = option.weightEnable
    }

    override fun onThemeChanged(pigment: Pigment?, fg: Int, bg: Int) {
        super.onThemeChanged(pigment, fg, bg)
        binding.contentGroup.setBackgroundColor(bg)
        binding.titleView.setTextColor(fg)

//        binding.slider.tickInactiveTintList = ColorStateList.valueOf(fg.changeAlpha(0.5F))
//        binding.slider.tickActiveTintList = ColorStateList.valueOf(fg)

        binding.slider.trackActiveTintList = ColorStateList.valueOf(fg)
        binding.slider.trackInactiveTintList = ColorStateList.valueOf(fg.changeAlpha(0.5F))

        binding.slider.haloTintList = ColorStateList.valueOf(fg.changeAlpha(0.24F))
        binding.slider.thumbTintList = ColorStateList.valueOf(fg)

        binding.weightModeSwitch.thumbTintList = ColorStateList.valueOf(fg)
        binding.weightModeSwitch.trackTintList = ColorStateList.valueOf(fg.changeAlpha(0.5F))
        binding.weightModeSwitch.setTextColor(fg)
    }

    override fun dismiss() {
        super.dismiss()
        val weightEnable = binding.weightModeSwitch.isChecked
        var value = binding.slider.value
        if (weightEnable) {
            value *= 0.01F
        }
        option.callback.onResult(weightEnable, value)
    }

    class Option(
        val context: Context,
        val title: CharSequence,
        val weightEnable: Boolean,
        val value: Float,
        val callback: Callback
    )

    fun interface Callback {
        fun onResult(weightEnable: Boolean, value: Float)
    }

}