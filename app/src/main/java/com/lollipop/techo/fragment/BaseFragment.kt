package com.lollipop.techo.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import com.lollipop.base.util.checkCallback
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentProvider


open class BaseFragment : Fragment(), PigmentPage {

    private var superPigmentProvider: PigmentProvider? = null

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        onBackPressedCallback = null
    }

    override fun onResume() {
        super.onResume()
        checkCallback<PigmentProvider>(context) {
            superPigmentProvider = it
            it.registerPigment(this)
        }
    }

    override fun onPause() {
        super.onPause()
        superPigmentProvider?.unregisterPigment(this)
        superPigmentProvider = null
    }

    override fun onDecorationChanged(pigment: Pigment) {
    }

    override val currentPigment: Pigment?
        get() {
            return superPigmentProvider?.pigmentProviderHelper?.currentPigment
        }

    protected fun notifyBackPressed() {
        onBackPressedCallback?.notifyFragmentBackPressed()
    }

    interface OnBackPressedCallback {
        fun notifyFragmentBackPressed()
    }

}