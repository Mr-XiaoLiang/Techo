package com.lollipop.web.impl.default

import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.webkit.SafeBrowsingResponseCompat
import androidx.webkit.WebViewClientCompat
import com.lollipop.web.IWeb
import com.lollipop.web.listener.SafeBrowsingHitCallback
import com.lollipop.web.listener.SafeBrowsingHitResponse
import com.lollipop.web.listener.TitleListener

class WebViewClientImpl(private val iWeb: IWeb) : WebViewClientCompat() {

    var titleListener: TitleListener? = null

    var safeBrowsingHitCallback: SafeBrowsingHitCallback? = null

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (iWeb.view === view) {
            titleListener?.onTitleChanged(iWeb, view.title ?: "")
        }
    }

    override fun onSafeBrowsingHit(
        view: WebView,
        request: WebResourceRequest,
        threatType: Int,
        callback: SafeBrowsingResponseCompat
    ) {
        val hitCallback = safeBrowsingHitCallback
        if (hitCallback != null) {
            hitCallback.onHit(view, request, SafeBrowsingHitResponse(callback))
        } else {
            super.onSafeBrowsingHit(view, request, threatType, callback)
        }
    }

}