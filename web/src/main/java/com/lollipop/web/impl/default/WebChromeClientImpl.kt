package com.lollipop.web.impl.default

import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.listener.GeolocationPermissionsListener
import com.lollipop.web.listener.GeolocationPermissionsResult
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener

class WebChromeClientImpl(private val iWeb: IWeb) : WebChromeClient() {

    var progressListener: ProgressListener? = null

    var titleListener: TitleListener? = null

    var geolocationPermissionsListener: GeolocationPermissionsListener? = null

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

}