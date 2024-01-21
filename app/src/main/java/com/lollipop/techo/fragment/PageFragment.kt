package com.lollipop.techo.fragment

import android.app.Activity
import android.content.Intent
import android.view.View
import com.lollipop.base.util.onClick

open class PageFragment : BaseFragment() {

    protected fun bindBackButton(view: View) {
        view.onClick {
            notifyBackPressed()
        }
    }

    protected fun setResult(code: Int, data: Intent? = null) {
        activity?.setResult(code, data)
    }

    protected fun setResultSuccess(data: Intent?) {
        setResult(Activity.RESULT_OK, data)
    }

    protected fun setResultCanceled(data: Intent?) {
        setResult(Activity.RESULT_CANCELED, data)
    }

}