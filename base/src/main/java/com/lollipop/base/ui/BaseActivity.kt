package com.lollipop.base.ui

import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.listener.BackPressListener
import com.lollipop.base.listener.OnInsetsChangeListener
import com.lollipop.base.provider.BackPressProvider
import com.lollipop.base.provider.WindowInsetsProvider
import com.lollipop.base.util.BackPressProviderHelper
import com.lollipop.base.util.WindowInsetsProviderHelper

/**
 * @author lollipop
 * @date 4/16/21 22:02
 * 基础的Activity
 * 提供基础的实现和能力
 */
open class BaseActivity : AppCompatActivity(),
        BackPressProvider,
        WindowInsetsProvider,
        OnInsetsChangeListener {

    private val backPressProviderHelper = BackPressProviderHelper()

    private val windowInsetsProviderHelper = WindowInsetsProviderHelper(getSelf())

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

    protected fun bindRootViewWithInsets(root: View) {
        root.fitsSystemWindows = true
        root.setOnApplyWindowInsetsListener { v, insets ->
            // TODO
            insets
        }
    }

    private fun dispatchInsets(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        windowInsetsProviderHelper.onInsetsChanged(root, left, top, right, bottom)
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

    override fun addInsetsChangeListener(listener: OnInsetsChangeListener) {
        windowInsetsProviderHelper.addInsetsChangeListener(listener)
    }

    override fun removeInsetsChangeListener(listener: OnInsetsChangeListener) {
        windowInsetsProviderHelper.removeInsetsChangeListener(listener)
    }

    override fun onInsetsChanged(root: View, left: Int, top: Int, right: Int, bottom: Int) {
    }

}