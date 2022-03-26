package com.lollipop.web.listener

import android.view.View

interface CustomViewListener {

    fun onShowCustomView(view: View?, callback: CustomViewCallback)

    fun onHideCustomView()

    interface CustomViewCallback {
        fun onCustomViewHidden()
    }

}