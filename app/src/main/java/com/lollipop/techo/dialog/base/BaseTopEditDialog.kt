package com.lollipop.techo.dialog.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.R
import com.lollipop.techo.databinding.DialogTopEditBinding

abstract class BaseTopEditDialog(context: Context) : Dialog(context) {

    protected abstract val contentView: View

    private val baseBinding: DialogTopEditBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setWindowAnimations(R.style.TopDialog)
        setContentView(baseBinding.root.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        })
        baseBinding.editCard.addView(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            contentView.layoutParams?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT
        )
        baseBinding.editCard.fixInsetsByMargin(WindowInsetsHelper.Edge.ALL)
    }

}