package com.lollipop.lqrdemo.creator.content

import android.content.Context
import com.lollipop.base.util.checkCallback
import com.lollipop.lqrdemo.base.BaseFragment

abstract class ContentBuilder : BaseFragment() {

    private var contentCallback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contentCallback = checkCallback<Callback>(context)
    }

    override fun onDetach() {
        super.onDetach()
        contentCallback = null
    }

    protected fun setContentResult(value: String) {
        contentCallback?.resultContentValue(value)
    }

    interface Callback {

        fun resultContentValue(value: String)

    }

}