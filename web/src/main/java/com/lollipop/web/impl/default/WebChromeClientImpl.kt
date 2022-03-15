package com.lollipop.web.impl.default

import android.webkit.WebChromeClient
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.listener.ProgressListener

class WebChromeClientImpl(private val iWeb: IWeb): WebChromeClient() {

    var progressListener: ProgressListener? = null

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (iWeb.view === view) {
            progressListener?.onProgressChanged(iWeb, newProgress)
        }
    }

}