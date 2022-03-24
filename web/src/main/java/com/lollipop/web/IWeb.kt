package com.lollipop.web

import android.view.View
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.listener.GeolocationPermissionsListener
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener
import com.lollipop.web.listener.WindowListener

interface IWeb {

    val host: WebHost

    val view: View

    fun addBridgeRoot(bridge: BridgeRoot)

    fun load(url: String)

    fun setProgressListener(listener: ProgressListener?)

    fun setTitleListener(listener: TitleListener?)

    fun setGeolocationPermissionsListener(listener: GeolocationPermissionsListener?)

    fun setWindowListener(listener: WindowListener?)

    val canGoBack: Boolean

    val canGoForward: Boolean

    fun goBack()

    fun goForward()

    fun goBackOrForward(steps: Int)

    fun setConfig(config: IWebConfig)

}