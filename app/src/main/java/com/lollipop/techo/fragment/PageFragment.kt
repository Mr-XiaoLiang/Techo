package com.lollipop.techo.fragment

import android.view.View
import com.lollipop.base.ui.BaseFragment
import com.lollipop.base.util.onClick

open class PageFragment : BaseFragment() {

    protected fun bindBackButton(view: View) {
        view.onClick {
            activity?.onBackPressed()
        }
    }

}