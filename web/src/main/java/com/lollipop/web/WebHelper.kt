package com.lollipop.web

import android.view.View

/**
 * 放弃继承和包装，采用组合组件的形式来提供Web的支持能力
 */
class WebHelper(private val host: WebHost, private val iWeb: IWeb) {

    companion object {
        fun bind(host: WebHost, view: View): WebHelper {
            return WebHelper(host, IWebFactory.create(host, view))
        }
    }

}