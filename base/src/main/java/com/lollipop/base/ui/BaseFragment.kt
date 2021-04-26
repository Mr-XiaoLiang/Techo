package com.lollipop.base.ui

import android.content.Context
import androidx.fragment.app.Fragment
import com.lollipop.base.listener.BackPressListener
import com.lollipop.base.provider.BackPressProvider
import com.lollipop.base.util.BackPressProviderHelper

/**
 * @author lollipop
 * @date 4/16/21 22:03
 * 基础的Fragment
 * 提供基础的实现和能力
 */
open class BaseFragment: Fragment(), BackPressListener, BackPressProvider {

    private val backPressProviderHelper = BackPressProviderHelper(getSelf())

    private var parentProvider: BackPressProvider? = null

    private fun getSelf(): BaseFragment {
        return this
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        check<BackPressProvider>(context) {
            parentProvider = it
            it.addBackPressListener(backPressProviderHelper)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressProviderHelper.destroy()
    }

    override fun onDetach() {
        super.onDetach()
        parentProvider?.removeBackPressListener(backPressProviderHelper)
        parentProvider = null
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun addBackPressListener(listener: BackPressListener) {
        backPressProviderHelper.addBackPressListener(listener)
    }

    override fun removeBackPressListener(listener: BackPressListener) {
        backPressProviderHelper.removeBackPressListener(listener)
    }

    protected inline fun <reified T: Any> check(
        context: Context? = null,
        callback: (T) -> Unit) {
        if (parentFragment?.check(callback) == true) {
            return
        }
        if (getContext()?.check(callback) == true) {
            return
        }
        if (context?.check(callback) == true) {
            return
        }
    }

    protected inline fun <reified T: Any> Any.check(callback: (T) -> Unit): Boolean {
        if (this is T) {
            callback(this)
            return true
        }
        return false
    }

}