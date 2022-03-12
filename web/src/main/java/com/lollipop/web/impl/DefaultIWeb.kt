package com.lollipop.web.impl

import android.view.View
import android.webkit.WebView
import com.lollipop.web.IWeb
import com.lollipop.web.WebHost

class DefaultIWeb(override val host: WebHost, val webView: WebView) : IWeb {

    override val view: View
        get() {
            return webView
        }

    fun add() {
    }

}