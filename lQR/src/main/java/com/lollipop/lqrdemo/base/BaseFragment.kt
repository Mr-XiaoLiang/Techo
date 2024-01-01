package com.lollipop.lqrdemo.base

import androidx.fragment.app.Fragment
import com.lollipop.base.util.checkCallback
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentProvider

open class BaseFragment : Fragment(), PigmentPage {

    private var superPigmentProvider: PigmentProvider? = null

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

}