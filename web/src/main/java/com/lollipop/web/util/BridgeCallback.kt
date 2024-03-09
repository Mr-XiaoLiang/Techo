package com.lollipop.web.util

import com.lollipop.web.IWeb
import com.lollipop.web.bridge.BridgePayload
import com.lollipop.web.compat.WebX
import org.json.JSONObject

object BridgeCallback {
    fun callbackWebMethod(
        web: IWeb,
        methodName: String,
        params: String,
        resultCallback: (String) -> Unit
    ) {
        val url = StringBuilder()
            .append("javascript:")
            .append(WebX.WEB_BRIDGE_NAME)
            .append("(\"").append(methodName).append("\"")
            .append(
                if (params.isBlank()) {
                    ""
                } else {
                    ",$params"
                }
            )
            .append(")")
            .toString()
        web.view.post {
            web.evaluateJavascript(url) { result ->
                resultCallback(result ?: "")
            }
        }
    }

    fun callbackWebMethod(
        web: IWeb,
        methodName: String,
        params: JSONObject?,
        resultCallback: (String) -> Unit
    ) {
        callbackWebMethod(web, methodName, params?.toString() ?: "", resultCallback)
    }

}

fun IWeb.callback(
    methodName: String,
    params: String,
    result: (String) -> Unit = {}
) {
    BridgeCallback.callbackWebMethod(this, methodName, params, result)
}

fun IWeb.callback(
    methodName: String,
    params: JSONObject? = null,
    result: (String) -> Unit = {}
) {
    BridgeCallback.callbackWebMethod(this, methodName, params, result)
}

fun IWeb.callback(
    payload: BridgePayload,
    params: String,
    result: (String) -> Unit = {}
) {
    val callback = payload.callback
    if (callback.isBlank()) {
        return
    }
    BridgeCallback.callbackWebMethod(this, callback, params, result)
}

fun IWeb.callback(
    payload: BridgePayload,
    params: JSONObject? = null,
    result: (String) -> Unit = {}
) {
    val callback = payload.callback
    if (callback.isBlank()) {
        return
    }
    BridgeCallback.callbackWebMethod(this, callback, params, result)
}
