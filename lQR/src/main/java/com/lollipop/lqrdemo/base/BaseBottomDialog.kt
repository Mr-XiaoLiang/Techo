package com.lollipop.lqrdemo.base

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.CallSuper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.WindowInsetsType
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.databinding.DialogBaseBottomBinding
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentProvider

abstract class BaseBottomDialog(context: Context) : BottomSheetDialog(context), PigmentPage {

    protected abstract val contentView: View

    private val binding: DialogBaseBottomBinding by lazyBind()

    protected var lastPigment: Pigment? = null

    override val currentPigment: Pigment?
        get() {
            return lastPigment
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.dialogContentGroup.addView(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.contentLayout.fixInsetsByPadding(WindowInsetsEdge.CONTENT).apply {
            windowInsetsOperator.insetsType = WindowInsetsType.SYSTEM_GESTURES
        }
        window?.let {
            WindowInsetsHelper.fitsSystemWindows(it)
            updateWindowAttributes(it)
        }
        findPigmentProvider()?.registerPigment(this)
        binding.root.post {
            findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun dismiss() {
        super.dismiss()
        findPigmentProvider()?.unregisterPigment(this)
    }

    private fun findPigmentProvider(): PigmentProvider? {
        var c = context
        while (true) {
            if (c is PigmentProvider) {
                return c
            }
            if (c is ContextWrapper) {
                c = c.baseContext
            } else {
                return null
            }
        }
    }

    private fun updateWindowAttributes(window: Window) {
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        window.attributes = window.attributes.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    @CallSuper
    override fun onDecorationChanged(pigment: Pigment) {
        lastPigment = pigment
        binding.contentLayout.setBackgroundColor(pigment.backgroundColor)
        binding.dialogTouchHolder.color = pigment.onBackgroundBody
    }

}