package com.lollipop.lqrdemo.creator.subpage.adjust

import android.content.Context
import com.lollipop.base.util.checkCallback
import com.lollipop.lqrdemo.base.BaseFragment

open class StyleAdjustContentFragment : BaseFragment() {

    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    protected fun notifyContentChanged() {
        callback?.notifyContentChanged()
    }

    interface Callback {

        fun notifyContentChanged()

    }

}