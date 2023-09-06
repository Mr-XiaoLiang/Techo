package com.lollipop.lqrdemo.creator

import android.content.Context
import android.os.Bundle
import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.BaseCenterDialog
import com.lollipop.lqrdemo.databinding.DialogQrCornerSettingBinding
import com.lollipop.pigment.Pigment

class QrCornerSettingDialog(context: Context) : BaseCenterDialog(context) {

    private val binding: DialogQrCornerSettingBinding by lazyBind()

    private val cutModeDrawable by lazy {
        QrCornerSettingHelper.cutModeDrawable()
    }

    private val  roundModeDrawable by lazy {
        QrCornerSettingHelper.roundModeDrawable()
    }

    private val  squircleModeDrawable by lazy {
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
    }

    override fun onDecorationChanged(pigment: Pigment?) {
        super.onDecorationChanged(pigment)
        // TODO
    }

    override fun onThemeChanged(fg: Int, bg: Int) {
        super.onThemeChanged(fg, bg)
        binding.contentGroup.setBackgroundColor(bg)
        // TODO
    }


}