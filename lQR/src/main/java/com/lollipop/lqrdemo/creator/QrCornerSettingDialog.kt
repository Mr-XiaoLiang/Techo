package com.lollipop.lqrdemo.creator

import android.content.Context
import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.BaseCenterDialog
import com.lollipop.lqrdemo.databinding.DialogQrCornerSettingBinding

class QrCornerSettingDialog(context: Context) : BaseCenterDialog(context) {

    private val binding: DialogQrCornerSettingBinding by lazyBind()
    override fun onCreateDialogView(): View {
        return binding.root
    }

    override fun onThemeChanged(fg: Int, bg: Int) {
        super.onThemeChanged(fg, bg)
        binding.contentGroup.setBackgroundColor(bg)
        // TODO
    }


}