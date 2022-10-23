package com.lollipop.techo.dialog

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.techo.R

open class BaseBottomSheetDialog(
    context: Context
) : BottomSheetDialog(context, R.style.Theme_Techo_BottomSheetDialog) {

    protected open val hideBackground = true

    override fun onStart() {
        super.onStart()
        if (hideBackground) {
            // 要命的background，竟然是在后面加上的
            findViewById<View>(R.id.design_bottom_sheet)?.let {
                it.post {
                    it.background = null
                }
            }
        }
    }

}