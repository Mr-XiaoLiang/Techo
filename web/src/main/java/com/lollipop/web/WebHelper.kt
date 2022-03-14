package com.lollipop.web

import android.view.View
import com.lollipop.web.bridge.Bridge

/**
 * 放弃继承和包装，采用组合组件的形式来提供Web的支持能力
 */
class WebHelper(private val host: WebHost, private val iWeb: IWeb) {

    companion object {

        private val globeBridge = ArrayList<Class<out Bridge>>()

        fun bind(host: WebHost, view: View): WebHelper {
            return WebHelper(host, IWebFactory.create(host, view))
        }

        fun register(clazz: Class<out Bridge>) {
            if (globeBridge.contains(clazz)) {
                return
            }
            globeBridge.add(clazz)
        }

        fun unregister(clazz: Class<out Bridge>) {
            globeBridge.remove(clazz)
        }

    }

    private var isInit = false

    fun addBridge(bridge: Bridge) {
        iWeb.addBridge(bridge)
    }

    fun init() {
        if (isInit) {
            return
        }
        isInit = true
        globeBridge.forEach {
            addBridge(it.newInstance())
        }
    }

    fun loadUrl(url: String) {
        init()
        iWeb.load(url)
    }

}