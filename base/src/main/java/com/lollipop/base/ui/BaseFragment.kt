package com.lollipop.base.ui

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.lollipop.base.listener.BackPressListener
import com.lollipop.base.provider.BackPressProvider
import com.lollipop.base.request.RequestHelper
import com.lollipop.base.request.RequestLauncher
import com.lollipop.base.util.BackPressProviderHelper
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentProvider
import com.lollipop.pigment.PigmentProviderHelper

/**
 * @author lollipop
 * @date 4/16/21 22:03
 * 基础的Fragment
 * 提供基础的实现和能力
 */
open class BaseFragment : Fragment(),
    BackPressListener,
    BackPressProvider,
    RequestLauncher,
    PigmentPage,
    PigmentProvider {

    private val backPressProviderHelper = BackPressProviderHelper(getSelf())

    override val pigmentProviderHelper = PigmentProviderHelper()

    override val requestHelper: RequestHelper by lazy {
        RequestHelper.with(this)
    }

    private var parentProvider: BackPressProvider? = null
    private var parentPigmentProvider: PigmentProvider? = null

    private fun getSelf(): BaseFragment {
        return this
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        check<BackPressProvider>(context) {
            parentProvider = it
            it.addBackPressListener(backPressProviderHelper)
        }
        check<PigmentProvider> {
            it.registerPigment(this)
            parentPigmentProvider = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressProviderHelper.destroy()
        parentPigmentProvider?.unregisterPigment(this)
        parentPigmentProvider = null
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

    protected inline fun <reified T : Any> check(
        context: Context? = null,
        callback: (T) -> Unit
    ) {
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

    protected inline fun <reified T : Any> Any.check(callback: (T) -> Unit): Boolean {
        if (this is T) {
            callback(this)
            return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        requestHelper.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requestHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        pigmentProviderHelper.onDecorationChanged(pigment)
    }

}