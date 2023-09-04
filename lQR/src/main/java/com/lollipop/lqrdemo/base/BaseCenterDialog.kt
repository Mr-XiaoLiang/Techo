package com.lollipop.lqrdemo.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentWallpaperCenter

abstract class BaseCenterDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onCreateDialogView()?.let {
            setContentView(it)
        }

        window?.let {
            it.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        onDecorationChanged(PigmentWallpaperCenter.pigment)
    }

    protected abstract fun onCreateDialogView(): View?


    protected open fun onDecorationChanged(pigment: Pigment?) {
        if (pigment == null) {
            onThemeChanged(Color.WHITE, Color.BLACK)
            return
        }
        onThemeChanged(
            pigment.backgroundColor,
            BlendMode.blend(pigment.onBackgroundTitle, pigment.primaryColor)
        )
    }

    protected open fun onThemeChanged(fg: Int, bg: Int) {

    }

}