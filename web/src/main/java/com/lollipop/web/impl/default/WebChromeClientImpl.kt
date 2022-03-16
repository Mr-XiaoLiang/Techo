package com.lollipop.web.impl.default

import android.webkit.WebChromeClient
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener

class WebChromeClientImpl(private val iWeb: IWeb) : WebChromeClient() {

    var progressListener: ProgressListener? = null

    var titleListener: TitleListener? = null

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

}