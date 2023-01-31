package com.lollipop.browser

import android.app.Application
import com.lollipop.browser.bridge.BridgeConfig
import com.lollipop.browser.web.WebStatusManager

class BrowserApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        BridgeConfig.init()
        WebStatusManager.resume(this)
    }

}