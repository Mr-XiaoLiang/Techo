package com.lollipop.web

import android.view.View
import com.lollipop.web.bridge.Bridge

interface IWeb {

    val host: WebHost

    val view: View

    fun addBridge(bridge: Bridge)

    fun load(url: String)

}