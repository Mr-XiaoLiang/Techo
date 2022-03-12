package com.lollipop.web.bridge

import com.lollipop.web.IWeb
import com.lollipop.web.WebHost

/**
 * 接口别名，为一个接口包装为另一个别名
 * 使得相似或者相同实现的Bridge可以被调用
 */
open class BridgeAlias(private val alias: String, val base: Bridge) : Bridge {

    override val name: String
        get() {
            return alias
        }

    override fun invoke(host: WebHost, web: IWeb, params: Array<String>) {
        if (base is AliasBridge) {
            base.invokeAlias(host, web, alias, params)
        } else {
            base.invoke(host, web, params)
        }
    }

    interface AliasBridge {
        fun invokeAlias(host: WebHost, web: IWeb, alias: String, params: Array<String>)
    }

}