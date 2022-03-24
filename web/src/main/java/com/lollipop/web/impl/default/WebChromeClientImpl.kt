package com.lollipop.web.impl.default

import android.os.Message
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.listener.*

class WebChromeClientImpl(private val iWeb: IWeb) : WebChromeClient() {

    var progressListener: ProgressListener? = null

    var titleListener: TitleListener? = null

    var geolocationPermissionsListener: GeolocationPermissionsListener? = null

    var windowListener: WindowListener? = null

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (iWeb.view === view) {
            progressListener?.onProgressChanged(iWeb, newProgress)
        }
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        if (iWeb.view === view) {
            titleListener?.onTitleChanged(iWeb, title ?: "")
        }
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        geolocationPermissionsListener?.request(
            origin ?: "",
            GeolocationPermissionsResultWrapper(callback)
        )
    }

    override fun onGeolocationPermissionsHidePrompt() {
        geolocationPermissionsListener?.abandon()
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (iWeb.view == view && resultMsg != null) {
            return windowListener?.onCrete(
                iWeb,
                isDialog,
                isUserGesture,
                WindowCreateResultWrapper(resultMsg)
            ) ?: false
        }
        return false
    }

    override fun onCloseWindow(window: WebView?) {
        if (iWeb.view == window) {
            windowListener?.onClose(iWeb)
        }
    }

    private class GeolocationPermissionsResultWrapper(
        private val callback: GeolocationPermissions.Callback?
    ) : GeolocationPermissionsResult {
        override fun onGeolocationPermissionsResult(
            origin: String,
            allow: Boolean,
            retain: Boolean
        ) {
            callback?.invoke(origin, allow, retain)
        }
    }

    private class WindowCreateResultWrapper(
        private val resultMsg: Message
    ) : WindowListener.WindowCreateResult {
        override fun onWebCreated(iWeb: IWeb) {
            val transport = resultMsg.obj
            val view = iWeb.view
            if (transport is WebView.WebViewTransport && view is WebView) {
                transport.webView = view
                resultMsg.sendToTarget()
            }
        }
    }

}