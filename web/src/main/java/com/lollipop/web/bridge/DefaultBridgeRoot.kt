package com.lollipop.web.bridge

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.lollipop.web.compat.WebX
import org.json.JSONObject

class DefaultBridgeRoot : BridgeRoot() {

    companion object {
        const val NAME = "Default"
    }

    override val name: String = NAME

    @JavascriptInterface
    @Keep
    fun nativeBridge(actionName: String, params: String?) {
        dispatch(parseParams(actionName, params) ?: return)
    }

    private fun parseParams(actionName: String, params: String?): BridgePayload? {
        if (actionName.isBlank()) {
            return null
        }
        if (params.isNullOrBlank()) {
            return BridgePayload(actionName, emptyMap(), "")
        }
        try {
            val paramsObj = JSONObject(params)
            val contentObj = paramsObj.optJSONObject(WebX.Bridge.DATA)
            val contentMap = HashMap<String, String>()
            contentObj?.keys()?.forEach {
                contentMap[it] = contentObj.optString(it)
            }
            val callbackName = paramsObj.optString(WebX.Bridge.CALLBACK)
            return BridgePayload(actionName, contentMap, callbackName)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return BridgePayload(actionName, emptyMap(), "")
    }

}