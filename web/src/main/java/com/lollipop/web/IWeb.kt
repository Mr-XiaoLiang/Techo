package com.lollipop.web

import android.view.View
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.listener.*

interface IWeb {

    val host: WebHost

    val view: View

    fun addBridgeRoot(bridge: BridgeRoot)

    fun load(url: String)

    fun setProgressListener(listener: ProgressListener?)

    fun setTitleListener(listener: TitleListener?)

    fun setGeolocationPermissionsListener(listener: GeolocationPermissionsListener?)

    fun setWindowListener(listener: WindowListener?)

    fun setHintProvider(provider: HintProvider?)

    fun setLogPrinter(printer: LogPrinter)

    fun setCustomViewListener(listener: CustomViewListener?)

    fun setDownloadListener(listener: DownloadListener?)

    val canGoBack: Boolean

    val canGoForward: Boolean

    fun goBack()

    fun goForward()

    fun goBackOrForward(steps: Int)

    fun setConfig(config: IWebConfig)

    fun onResume()

    fun onPause()

    fun onDestroy()

}