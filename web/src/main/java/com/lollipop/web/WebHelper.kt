package com.lollipop.web

import android.view.View
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgeRoot

/**
 * 放弃继承和包装，采用组合组件的形式来提供Web的支持能力
 */
class WebHelper(private val iWeb: IWeb) {

    companion object {

        private val globeBridgeRoot = ArrayList<Class<out BridgeRoot>>()
        private val globeBridge = HashMap<String, ArrayList<Class<out Bridge>>>()

        fun bind(host: WebHost, view: View): WebHelper {
            return WebHelper(IWebFactory.create(host, view))
        }

        fun register(clazz: Class<out BridgeRoot>) {
            if (globeBridgeRoot.contains(clazz)) {
                return
            }
            globeBridgeRoot.add(clazz)
        }

        fun register(rootName: String, clazz: Class<out Bridge>) {
            val bridgeList = globeBridge[rootName] ?: ArrayList()
            if (bridgeList.contains(clazz)) {
                return
            }
            bridgeList.add(clazz)
            globeBridge[rootName] = bridgeList
        }

        fun unregister(clazz: Class<out BridgeRoot>) {
            globeBridgeRoot.remove(clazz)
        }

        fun unregister(rootName: String, clazz: Class<out Bridge>) {
            val bridgeList = globeBridge[rootName] ?: return
            bridgeList.remove(clazz)
        }

    }

    private var isInit = false

    val canRegisterBridgeRoot: Boolean
        get() {
            return !isInit
        }

    private val bridgeRootList = ArrayList<BridgeRoot>()

    fun addBridgeRoot(bridge: BridgeRoot) {
        iWeb.addBridgeRoot(bridge)
    }

    fun addBridge(rootName: String, bridge: Bridge) {
        this.bridgeRootList.forEach {
            if (it.name == rootName) {
                it.addBridge(bridge)
                return
            }
        }
    }

    fun init() {
        if (isInit) {
            return
        }
        isInit = true
        globeBridgeRoot.forEach {
            addBridgeRoot(it.newInstance())
        }
        globeBridge.forEach { entry ->
            val key = entry.key
            this.bridgeRootList.find { it.name == key }?.let { root ->
                entry.value.forEach { bridge ->
                    root.addBridge(bridge.newInstance())
                }
            }
        }
    }

    fun loadUrl(url: String) {
        init()
        iWeb.load(url)
    }

}