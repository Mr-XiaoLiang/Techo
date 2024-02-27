package com.lollipop.techo.dialog

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.techo.R
import com.lollipop.techo.data.AppTheme

open class BaseBottomSheetDialog(
    context: Context
) : BottomSheetDialog(context, R.style.Theme_Techo_BottomSheetDialog), PigmentPage {

    protected open val hideBackground = true

    private var dialogPigment: Pigment? = null

    override val currentPigment: Pigment?
        get() {
            return dialogPigment
        }

    fun setPigment(pigment: Pigment) {
        this.dialogPigment = pigment
    }

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
        updatePigment()
    }

    private fun updatePigment() {
        var pigment = currentPigment
        if (pigment == null) {
            val c = context
            if (c is PigmentPage) {
                pigment = c.currentPigment
            }
        }
        if (pigment == null) {
            pigment = AppTheme.current
        }
        onDecorationChanged(pigment)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        dialogPigment = pigment
    }

}