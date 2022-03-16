package com.lollipop.web.impl.default

import android.webkit.WebView
import android.webkit.WebViewClient
import com.lollipop.web.IWeb
import com.lollipop.web.listener.TitleListener

class WebViewClientImpl(private val iWeb: IWeb) : WebViewClient() {

    var titleListener: TitleListener? = null

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (iWeb.view === view) {
            titleListener?.onTitleChanged(iWeb, view.title ?: "")
        }
    }

}