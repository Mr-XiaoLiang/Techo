package com.lollipop.web

import android.view.View
import android.webkit.WebView
import com.lollipop.web.impl.DefaultIWeb
import java.lang.RuntimeException

object IWebFactory {

    fun create(host: WebHost, view: View): IWeb {
        when (view) {
            is WebView -> {
                return DefaultIWeb(host, view)
            }
            else -> {
                throw RuntimeException("未知的WebView")
            }
        }
    }

}