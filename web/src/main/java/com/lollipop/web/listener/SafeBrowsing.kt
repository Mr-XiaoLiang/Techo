package com.lollipop.web.listener

import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.webkit.SafeBrowsingResponseCompat
import androidx.webkit.WebViewFeature

interface SafeBrowsingHitCallback {
    fun onHit(
        view: WebView,
        request: WebResourceRequest,
        response: SafeBrowsingHitResponse
    )
}

class SafeBrowsingHitResponse(
    private val callback: SafeBrowsingResponseCompat
) {

    /**
     * 显示默认插页式提示。
     * @param allowReporting – 如果插页式提示应显示报告复选框，则为 true。
     */
    fun showInterstitial(allowReporting: Boolean, notSupported: () -> Unit) {
        if (WebViewFeature.isFeatureSupported(
                WebViewFeature.SAFE_BROWSING_RESPONSE_SHOW_INTERSTITIAL
            )
        ) {
            callback.showInterstitial(allowReporting)
        } else {
            notSupported()
        }
    }

    /**
     * 就等同于用户单击了“访问这个不安全的站点”按钮一样。
     * @param report – true 启用安全浏览报告。
     */
    fun proceed(report: Boolean, notSupported: () -> Unit) {
        if (WebViewFeature.isFeatureSupported(
                WebViewFeature.SAFE_BROWSING_RESPONSE_PROCEED
            )
        ) {
            callback.proceed(report)
        } else {
            notSupported()
        }
    }

    /**
     * 等同于用户单击“返回安全”按钮一样。
     * @param report – true 启用安全浏览报告。
     */
    fun backToSafety(report: Boolean, notSupported: () -> Unit) {
        if (WebViewFeature.isFeatureSupported(
                WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY
            )
        ) {
            callback.backToSafety(report)
        } else {
            notSupported()
        }
    }
}
