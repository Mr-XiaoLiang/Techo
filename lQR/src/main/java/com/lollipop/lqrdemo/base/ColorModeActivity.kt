package com.lollipop.lqrdemo.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.insets.WindowInsetsHelper

open class ColorModeActivity: AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        // 需要按照是否夜间模式来处理，等到做完夜间模式的适配，需要调整
        WindowInsetsHelper.getController(this).isAppearanceLightStatusBars = true
    }

    protected fun bindByBack(vararg view: View) {
        view.forEach {
            it.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

}