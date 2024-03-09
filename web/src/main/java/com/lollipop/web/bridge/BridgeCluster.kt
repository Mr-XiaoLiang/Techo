package com.lollipop.web.bridge

import com.lollipop.web.IWeb
import com.lollipop.web.WebHost

/**
 * 集束桥，可以将多个Bridge实现通过统一的接口按照一定的规则进行分发
 */
abstract class BridgeCluster : Bridge {

    protected val bridgeList = ArrayList<Bridge>()

    override fun invoke(host: WebHost, web: IWeb, payload: BridgePayload) {
        when (val intercept = intercept(host, web, payload)) {
            is BridgeIntercept.Dispatch -> {
                dispatch(host, web, intercept.newAction, payload)
            }

            BridgeIntercept.Pass -> {
                dispatch(host, web, payload.action, payload)
            }

            BridgeIntercept.Rejected -> {
                // 不做处理
            }
        }
    }

    protected fun dispatch(host: WebHost, web: IWeb, name: String, payload: BridgePayload) {
        bridgeList.forEach {
            if (it.name == name) {
                it.invoke(host, web, payload)
            }
        }
    }

    /**
     * 过滤Bridge
     * @return 返回的名称为指定响应的Bridge名称，如果找不到，那么将不会进行响应，如果返回为空，表示不做拦截
     */
    protected abstract fun intercept(
        host: WebHost,
        web: IWeb,
        payload: BridgePayload
    ): BridgeIntercept

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