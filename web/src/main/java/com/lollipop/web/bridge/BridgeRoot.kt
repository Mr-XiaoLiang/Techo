package com.lollipop.web.bridge

import com.lollipop.web.IWeb
import com.lollipop.web.WebHost
import java.lang.ref.WeakReference

abstract class BridgeRoot : BridgeCluster() {

    companion object {
        private val EMPTY_HOST = EmptyWebHost()

        fun simpleRoot(name: String): BridgeRoot {
            return Simple(name)
        }

    }

    private var hostReference: WeakReference<WebHost>? = null
    private var iWebReference: WeakReference<IWeb>? = null

    fun bind(host: WebHost, iWeb: IWeb) {
        hostReference = WeakReference(host)
        iWebReference = WeakReference(iWeb)
    }

    override fun intercept(host: WebHost, web: IWeb, params: Array<String>): String {
        return ""
    }

    protected fun dispatch(bridgeName: String, params: Array<String>) {
        val host = hostReference?.get() ?: EMPTY_HOST
        val iWeb = iWebReference?.get() ?: return
        dispatch(host, iWeb, bridgeName, params)
    }

    private class EmptyWebHost : WebHost

    class Simple(override val name: String) : BridgeRoot()

}