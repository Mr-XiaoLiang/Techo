package com.lollipop.lqrdemo.floating.view

import android.content.Context
import android.view.View

interface FloatingView {

    fun createView(context: Context, widthDp: Int, heightDp: Int, berthWeight: Float): View

    fun onBerthChanged(berth: FloatingViewBerth)

    fun onStateChanged(state: FloatingViewState)

}