package com.lollipop.base.ui

import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.listener.BackPressListener
import com.lollipop.base.provider.BackPressProvider
import com.lollipop.base.util.BackPressProviderHelper

/**
 * @author lollipop
 * @date 4/16/21 22:02
 * 基础的Activity
 * 提供基础的实现和能力
 */
open class BaseActivity : AppCompatActivity(), BackPressProvider{

    private val backPressProviderHelper = BackPressProviderHelper()

    private fun getSelf(): BaseActivity {
        return this
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyUp(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.isTracking
                && !event.isCanceled) {
            if (dispatchBackEvent()) {
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun dispatchBackEvent(): Boolean {
        return backPressProviderHelper.onBackPressed()
    }

    override fun addBackPressListener(listener: BackPressListener) {
        backPressProviderHelper.addBackPressListener(listener)
    }

    override fun removeBackPressListener(listener: BackPressListener) {
        backPressProviderHelper.removeBackPressListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressProviderHelper.destroy()
    }

}