package com.lollipop.web

import android.view.View
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.listener.ProgressListener

interface IWeb {

    val host: WebHost

    val view: View

    fun addBridgeRoot(bridge: BridgeRoot)

    fun load(url: String)

    fun setProgressListener(listener: ProgressListener?)

}