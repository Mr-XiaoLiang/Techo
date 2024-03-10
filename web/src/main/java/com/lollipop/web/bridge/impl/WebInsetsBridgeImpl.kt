package com.lollipop.web.bridge.impl

import android.view.View
import androidx.core.view.WindowInsetsCompat
import com.lollipop.web.IWeb
import com.lollipop.web.WebHost
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgePayload
import com.lollipop.web.util.callback
import org.json.JSONObject

class WebInsetsBridgeImpl : Bridge {

    companion object {
        var actionBarSize: Int = 0
    }

    object Result {
        const val ACTION_BAR_SIZE = "actionBarSize"
        const val LEFT_EDGE = "leftEdge"
        const val TOP_EDGE = "topEdge"
        const val RIGHT_EDGE = "rightEdge"
        const val BOTTOM_EDGE = "bottomEdge"
    }

    override val name: String = "getWebInsets"

    override fun invoke(host: WebHost, web: IWeb, payload: BridgePayload) {
        val view = web.view
        view.post {
            getInsets(host, web, payload, view)
        }
    }

    private fun getInsets(host: WebHost, web: IWeb, payload: BridgePayload, view: View) {
        val windowInsets = view.rootWindowInsets
        if (windowInsets == null) {
            web.callback(payload, resultInfo(actionBarSize, 0, 0, 0, 0))
            return
        }
        val insets = WindowInsetsCompat.toWindowInsetsCompat(windowInsets)
            .getInsets(WindowInsetsCompat.Type.systemBars())
        web.callback(
            payload,
            resultInfo(actionBarSize, insets.left, insets.top, insets.right, insets.bottom)
        )
    }

    private fun resultInfo(
        actionBarSize: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): JSONObject {
        return JSONObject().put(Result.ACTION_BAR_SIZE, actionBarSize)
            .put(Result.LEFT_EDGE, left)
            .put(Result.TOP_EDGE, top)
            .put(Result.RIGHT_EDGE, right)
            .put(Result.BOTTOM_EDGE, bottom)
    }

}