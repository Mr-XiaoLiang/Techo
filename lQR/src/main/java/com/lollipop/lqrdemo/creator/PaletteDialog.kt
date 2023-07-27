package com.lollipop.lqrdemo.creator

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.databinding.DialogPaletteBinding

class PaletteDialog(context: Context, selectedColorCallback: (Int) -> Unit) : Dialog(context) {

    companion object {
        fun show(context: Context, callback: (Int) -> Unit) {
            PaletteDialog(context, callback).show()
        }
    }

    private val binding: DialogPaletteBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

}