package com.lollipop.web.bridge

import com.lollipop.web.IWeb
import com.lollipop.web.WebHost

/**
 * 桥，沟通JS与Java的桥梁
 * 它是一个通用的接口，它需要具体的实现方来触发
 */
interface Bridge {

    /**
     * 接口的名称
     */
    val name: String

    /**
     * 接口的实现方法
     * @param host 上下文信息
     * @param web Web容器的对象
     * @param payload 函数的请求参数，以及要求的回调地址
     *
     * 但是对于直接注册的JavascriptInterface而言，它是不一样的
     * 并不能直接使用addJavascriptInterface时期生效，他只是一个规范后的工具类
     */
    fun invoke(host: WebHost, web: IWeb, payload: BridgePayload)

}