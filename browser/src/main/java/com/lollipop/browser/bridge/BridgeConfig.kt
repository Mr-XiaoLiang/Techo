package com.lollipop.browser.bridge

import com.lollipop.web.WebHelper
import com.lollipop.web.bridge.BridgeRoot

object BridgeConfig {

    private val bridgeRootArray: Array<Class<out BridgeRoot>> = arrayOf(
        // TODO
    )

    fun init() {
        bridgeRootArray.forEach {
            WebHelper.register(it)
        }
    }

}