package com.lollipop.web.impl.default

import android.graphics.Bitmap
import android.os.Message
import android.webkit.*
import com.lollipop.web.IWeb
import com.lollipop.web.listener.*

class WebChromeClientImpl(private val iWeb: IWeb) : WebChromeClient() {

    var progressListener: ProgressListener? = null

    var titleListener: TitleListener? = null

    var geolocationPermissionsListener: GeolocationPermissionsListener? = null

    var windowListener: WindowListener? = null

    var hintProvider: HintProvider? = null

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (iWeb.view === view) {
            progressListener?.onProgressChanged(iWeb, newProgress)
        }
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        if (iWeb.view === view) {
            titleListener?.onTitleChanged(iWeb, title ?: "")
        }
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        if (iWeb.view === view) {
            titleListener?.onIconChanged(iWeb, icon)
        }
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        super.onPermissionRequest(request)
    }

    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        super.onPermissionRequestCanceled(request)
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        if (view === iWeb.view) {
            return hintProvider?.prompt(
                iWeb,
                url ?: "",
                message ?: "",
                defaultValue ?: "",
                PromptResultWrapper(result)
            ) ?: super.onJsPrompt(view, url, message, defaultValue, result)
        }
        return false
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (view === iWeb.view) {
            return hintProvider?.alert(
                iWeb,
                url ?: "",
                message ?: "",
                ConfirmResultWrapper(result)
            ) ?: super.onJsAlert(view, url, message, result)
        }
        return false
    }

    override fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (view === iWeb.view) {
            return hintProvider?.beforeUnload(
                iWeb,
                url ?: "",
                message ?: "",
                ConfirmResultWrapper(result)
            ) ?: super.onJsBeforeUnload(view, url, message, result)
        }
        return false
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (view === iWeb.view) {
            return hintProvider?.confirm(
                iWeb,
                url ?: "",
                message ?: "",
                ConfirmResultWrapper(result)
            ) ?: super.onJsConfirm(view, url, message, result)
        }
        return false
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        geolocationPermissionsListener?.request(
            origin ?: "",
            GeolocationPermissionsResultWrapper(callback)
        )
    }

    override fun onGeolocationPermissionsHidePrompt() {
        geolocationPermissionsListener?.abandon()
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (iWeb.view == view && resultMsg != null) {
            return windowListener?.onCrete(
                iWeb,
                isDialog,
                isUserGesture,
                WindowCreateResultWrapper(resultMsg)
            ) ?: false
        }
        return false
    }

    override fun onCloseWindow(window: WebView?) {
        if (iWeb.view == window) {
            windowListener?.onClose(iWeb)
        }
    }

    private class GeolocationPermissionsResultWrapper(
        private val callback: GeolocationPermissions.Callback?
    ) : GeolocationPermissionsResult {
        override fun onGeolocationPermissionsResult(
            origin: String,
            allow: Boolean,
            retain: Boolean
        ) {
            callback?.invoke(origin, allow, retain)
        }
    }

    private class WindowCreateResultWrapper(
        private val resultMsg: Message
    ) : WindowListener.WindowCreateResult {
        override fun onWebCreated(iWeb: IWeb) {
            val transport = resultMsg.obj
            val view = iWeb.view
            if (transport is WebView.WebViewTransport && view is WebView) {
                transport.webView = view
                resultMsg.sendToTarget()
            }
        }
    }

    private class PromptResultWrapper(
        private val jsResult: JsPromptResult?
    ) : HintProvider.PromptResult {
        override fun cancel() {
            jsResult?.cancel()
        }

        override fun confirm(result: String) {
            jsResult?.confirm(result)
        }
    }

    private class ConfirmResultWrapper(
        private val jsResult: JsResult?
    ) : HintProvider.AlertResult, HintProvider.ConfirmResult {
        override fun cancel() {
            jsResult?.cancel()
        }

        override fun confirm() {
            jsResult?.confirm()
        }
    }

}