package com.lollipop.browser.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.databinding.DialogBaseBottomSheetBinding

open class BaseBottomSheetDialog(context: Context) : BottomSheetDialog(context) {

    private val baseBinding: DialogBaseBottomSheetBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(baseBinding.root)
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(
            LayoutInflater.from(context).inflate(
                layoutResID, baseBinding.sheetContentGroup, false
            )
        )
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        baseBinding.sheetContentGroup.removeAllViews()
        val layoutParams = params ?: view.layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        baseBinding.sheetContentGroup.addView(view, layoutParams)
    }

    override fun setContentView(view: View) {
        setContentView(view, null)
    }

    protected fun setContentView(binding: ViewBinding) {
        setContentView(binding.root)
    }

}