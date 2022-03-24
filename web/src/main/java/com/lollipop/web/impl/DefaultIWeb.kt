package com.lollipop.web.impl

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.IWebConfig
import com.lollipop.web.WebHost
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.impl.default.WebChromeClientImpl
import com.lollipop.web.impl.default.WebViewClientImpl
import com.lollipop.web.listener.GeolocationPermissionsListener
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener
import com.lollipop.web.listener.WindowListener

class DefaultIWeb(override val host: WebHost, val webView: WebView) : IWeb {

    override val view: View
        get() {
            return webView
        }

    private val chromeClientImpl = WebChromeClientImpl(this)
    private val viewClientImpl = WebViewClientImpl(this)

    init {
        webView.webChromeClient = chromeClientImpl
        webView.webViewClient = viewClientImpl
    }

    @SuppressLint("JavascriptInterface")
    override fun addBridgeRoot(bridge: BridgeRoot) {
        webView.addJavascriptInterface(bridge, bridge.name)
    }

    override fun load(url: String) {
        webView.loadUrl(url)
    }

    override fun setProgressListener(listener: ProgressListener?) {
        chromeClientImpl.progressListener = listener
    }

    override fun setTitleListener(listener: TitleListener?) {
        chromeClientImpl.titleListener = listener
        viewClientImpl.titleListener = listener
    }

    override fun setGeolocationPermissionsListener(listener: GeolocationPermissionsListener?) {
        chromeClientImpl.geolocationPermissionsListener = listener
    }

    override fun setWindowListener(listener: WindowListener?) {
        chromeClientImpl.windowListener = listener
    }

    override val canGoBack: Boolean
        get() {
            return webView.canGoBack()
        }

    override val canGoForward: Boolean
        get() {
            return webView.canGoForward()
        }

    override fun goBack() {
        webView.goBack()
    }

    override fun goForward() {
        webView.goForward()
    }

    override fun goBackOrForward(steps: Int) {
        webView.goBackOrForward(steps)
    }

    override fun setConfig(config: IWebConfig) {
        TODO("Not yet implemented")
    }

}