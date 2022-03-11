package com.lollipop.web

import android.view.View
import android.webkit.WebView
import com.lollipop.web.impl.DefaultIWeb
import java.lang.RuntimeException

object IWebFactory {

    fun create(view: View): IWeb {
        when (view) {
            is WebView -> {
                return DefaultIWeb(view)
            }
            else -> {
                throw RuntimeException("未知的WebView")
            }
        }
    }

}