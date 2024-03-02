package com.lollipop.techo.dialog

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.techo.R
import com.lollipop.techo.data.AppTheme
import com.lollipop.techo.data.TechoTheme

open class BaseBottomSheetDialog(
    context: Context
) : BottomSheetDialog(context, R.style.Theme_Techo_BottomSheetDialog) {

    protected open val hideBackground = true

    protected var dialogTheme: TechoTheme.Snapshot? = null
        private set

    fun setTheme(pigment: Pigment) {
        setTheme(TechoTheme.valueOf(pigment))
    }

    fun setTheme(snapshot: TechoTheme.Snapshot) {
        this.dialogTheme = snapshot
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
        updateTheme()
    }

    private fun updateTheme() {
        var theme = dialogTheme
        if (theme == null) {
            val c = context
            if (c is PigmentPage) {
                val pigment = c.currentPigment
                if (pigment != null) {
                    theme = TechoTheme.valueOf(pigment)
                }
            }
        }
        if (theme == null) {
            theme = TechoTheme.valueOf(AppTheme.current)
        }
        dialogTheme = theme
        onDecorationChanged(theme)
    }

    protected open fun onDecorationChanged(snapshot: TechoTheme.Snapshot) {
        // 这是为了兼容特殊快照主题的方法
    }

}