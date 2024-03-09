package com.lollipop.web.bridge

sealed class BridgeIntercept {

    /**
     * 拒绝本次请求的内容，不做分发也不做回调响应
     */
    data object Rejected : BridgeIntercept()

    /**
     * 分发为新的函数名
     */
    class Dispatch(val newAction: String) : BridgeIntercept()

    /**
     * 通过，使用原有的函数名分发
     */
    data object Pass : BridgeIntercept()

}