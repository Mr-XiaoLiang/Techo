package com.lollipop.web.impl

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.IWebConfig
import com.lollipop.web.IWebConfig.CacheMode.*
import com.lollipop.web.IWebConfig.ForceDark.*
import com.lollipop.web.IWebConfig.MixedContentMode.*
import com.lollipop.web.WebHost
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.impl.default.WebChromeClientImpl
import com.lollipop.web.impl.default.WebViewClientImpl
import com.lollipop.web.listener.*

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

    override fun setHintProvider(provider: HintProvider?) {
        chromeClientImpl.hintProvider = provider
    }

    override fun setLogPrinter(printer: LogPrinter) {
        chromeClientImpl.logPrinter = printer
    }

    override fun setCustomViewListener(listener: CustomViewListener?) {
        chromeClientImpl.customViewListener = listener
    }

    override fun setDownloadListener(listener: DownloadListener?) {
        if (listener == null) {
            webView.setDownloadListener(null)
        } else {
            webView.setDownloadListener(DownloadListenerWrapper(listener))
        }
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
        webView.settings.apply {
            javaScriptEnabled = config.javaScriptEnabled
            useWideViewPort = config.useWideViewPort
            loadWithOverviewMode = config.loadWithOverviewMode
            setSupportZoom(config.supportZoom)
            builtInZoomControls = config.builtInZoomControls
            displayZoomControls = config.displayZoomControls
            allowFileAccess = config.allowFileAccess
            javaScriptCanOpenWindowsAutomatically = config.javaScriptCanOpenWindowsAutomatically
            loadsImagesAutomatically = config.loadsImagesAutomatically
            defaultTextEncodingName = config.defaultTextEncodingName
            allowContentAccess = config.allowContentAccess
            blockNetworkImage = config.blockNetworkImage
            blockNetworkLoads = config.blockNetworkLoads
            cursiveFontFamily = config.cursiveFontFamily
            fantasyFontFamily = config.fantasyFontFamily
            fixedFontFamily = config.fixedFontFamily
            sansSerifFontFamily = config.sansSerifFontFamily
            serifFontFamily = config.serifFontFamily
            standardFontFamily = config.standardFontFamily
            databaseEnabled = config.databaseEnabled
            defaultFixedFontSize = config.defaultFixedFontSize
            defaultFontSize = config.defaultFontSize
            minimumFontSize = config.minimumFontSize
            minimumLogicalFontSize = config.minimumLogicalFontSize
            domStorageEnabled = config.domStorageEnabled
            setGeolocationEnabled(config.geolocationEnabled)
            mediaPlaybackRequiresUserGesture = config.mediaPlaybackRequiresUserGesture
            setNeedInitialFocus(config.needInitialFocus)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                offscreenPreRaster = config.offscreenPreRaster
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = config.safeBrowsingEnabled
            }
            setSupportMultipleWindows(config.supportMultipleWindows)
            textZoom = config.textZoom
            cacheMode = when (config.cacheMode) {
                DEFAULT -> {
                    WebSettings.LOAD_DEFAULT
                }
                CACHE_ELSE_NETWORK -> {
                    WebSettings.LOAD_CACHE_ELSE_NETWORK
                }
                NO_CACHE -> {
                    WebSettings.LOAD_NO_CACHE
                }
                CACHE_ONLY -> {
                    WebSettings.LOAD_CACHE_ONLY
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                forceDark = when (config.forceDark) {
                    OFF -> {
                        WebSettings.FORCE_DARK_OFF
                    }
                    AUTO -> {
                        WebSettings.FORCE_DARK_AUTO
                    }
                    ON -> {
                        WebSettings.FORCE_DARK_ON
                    }
                }
            }
            mixedContentMode = when (config.mixedContentMode) {
                NEVER_ALLOW -> {
                    WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
                ALWAYS_ALLOW -> {
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                COMPATIBILITY_MODE -> {
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }
            }
        }
    }

    override fun onResume() {
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
    }

    private class DownloadListenerWrapper(
        private val listener: DownloadListener
    ) : android.webkit.DownloadListener {
        override fun onDownloadStart(
            url: String?,
            userAgent: String?,
            contentDisposition: String?,
            mimetype: String?,
            contentLength: Long
        ) {
            listener.createDownload(
                url ?: "",
                userAgent ?: "",
                contentDisposition ?: "",
                mimetype ?: "",
                contentLength
            )
        }

    }

}