package com.lollipop.web.impl

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.WebHost
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.impl.default.WebChromeClientImpl
import com.lollipop.web.impl.default.WebViewClientImpl
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener

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

}