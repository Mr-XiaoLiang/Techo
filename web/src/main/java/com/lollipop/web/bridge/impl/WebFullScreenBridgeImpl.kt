package com.lollipop.web.bridge.impl

import com.lollipop.web.IWeb
import com.lollipop.web.WebHost
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgePayload

class WebFullScreenBridgeImpl : Bridge {

    override val name: String = "fullScreen"

    override fun invoke(host: WebHost, web: IWeb, payload: BridgePayload) {
        val delegate = PayloadDelegate(payload)
        if (host is Callback) {
            host.setFullScreenMode(delegate)
        }
    }

    class PayloadDelegate(payload: BridgePayload) : BridgePayload.Delegate(payload) {
        val fixStatusBar by boolean()
        val fixNavigateBar by boolean()
    }

    interface Callback {

        fun setFullScreenMode(delegate: PayloadDelegate)

    }

}