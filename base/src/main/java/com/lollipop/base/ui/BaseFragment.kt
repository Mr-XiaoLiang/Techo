package com.lollipop.base.ui

import android.content.Context
import android.content.Intent
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.fragment.app.Fragment
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.listener.BackPressListener
import com.lollipop.base.request.RequestHelper
import com.lollipop.base.request.RequestLauncher
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
    RequestLauncher,
    PigmentPage,
    PigmentProvider {

    override val pigmentProviderHelper = PigmentProviderHelper()

    override val requestHelper: RequestHelper by lazy {
        RequestHelper.with(this)
    }

    protected val backPressHandler = BackPressHandler(false, getSelf())

    private var parentPigmentProvider: PigmentProvider? = null

    private fun getSelf(): BaseFragment {
        return this
    }

    override fun requestPigment(page: PigmentPage) {
        parentPigmentProvider?.requestPigment(page)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        check<PigmentProvider>(context) {
            it.registerPigment(this)
            parentPigmentProvider = it
        }
        check<OnBackPressedDispatcherOwner>(context) {
            backPressHandler.bindTo(it, this)
        }
    }

    override fun onDetach() {
        super.onDetach()
        backPressHandler.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        parentPigmentProvider?.unregisterPigment(this)
        parentPigmentProvider = null
    }

    override fun onBackPressed() {
    }

    fun notifyBackPress() {
        activity?.onBackPressedDispatcher?.onBackPressed()
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