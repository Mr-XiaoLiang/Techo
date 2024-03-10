package com.lollipop.web.bridge.impl

import com.lollipop.web.IWeb
import com.lollipop.web.WebHost
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgePayload

class WebFullScreenBridgeImpl: Bridge {

    override val name: String = "fullScreen"

    override fun invoke(host: WebHost, web: IWeb, payload: BridgePayload) {
        TODO("Not yet implemented")
    }
}