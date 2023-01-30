package com.lollipop.web.bridge

import com.lollipop.web.IWeb
import com.lollipop.web.WebHost

/**
 * 集束桥，可以将多个Bridge实现通过统一的接口按照一定的规则进行分发
 */
abstract class BridgeCluster : Bridge {

    protected val bridgeList = ArrayList<Bridge>()

    override fun invoke(host: WebHost, web: IWeb, params: Array<String>) {
        val methodName = intercept(host, web, params)
        dispatch(host, web, methodName, params)
    }

    protected fun dispatch(host: WebHost, web: IWeb, name: String, params: Array<String>) {
        bridgeList.forEach {
            if (name.isEmpty() || it.name == name) {
                it.invoke(host, web, params)
            }
        }
    }

    /**
     * 过滤Bridge
     * @return 返回的名称为指定响应的Bridge名称，如果找不到，那么将不会进行响应，如果返回为空，表示不做拦截
     */
    protected abstract fun intercept(host: WebHost, web: IWeb, params: Array<String>): String

    fun addBridge(bridge: Bridge) {
        this.bridgeList.add(bridge)
    }

    fun removeBridge(bridge: Bridge) {
        this.bridgeList.remove(bridge)
    }

    fun removeBridge(name: String) {
        val iterator = this.bridgeList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.name == name) {
                iterator.remove()
            }
        }
    }

}