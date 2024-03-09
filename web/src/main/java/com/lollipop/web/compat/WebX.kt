package com.lollipop.web.compat

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.webkit.ValueCallback
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

object WebX {

    const val WEB_BRIDGE_NAME = "lWebBridge"

    object Bridge {
        const val DATA = "data"
        const val CALLBACK = "callback"
    }

    fun getEnginePackage(context: Context): PackageInfo? {
        return WebViewCompat.getCurrentWebViewPackage(context.applicationContext)
    }

    /**
     * 获取引擎的版本号
     */
    fun getEngineVersion(context: Context): EngineVersion {
        val enginPackage = getEnginePackage(context) ?: return EngineVersion.EMPTY
        val versionName = enginPackage.versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            enginPackage.longVersionCode
        } else {
            enginPackage.versionCode.toLong()
        }
        return EngineVersion(versionName, versionCode)
    }

    /**
     * 开始安全浏览初始化。
     * 在使用 true 调用回调之前，不能保证 URL 加载受到安全浏览的保护。
     * 并非所有设备都完全支持安全浏览。 对于那些设备，回调将接收到 false。
     * 如果清单标记或 android.webkit.WebSettings.setSafeBrowsingEnabled 已禁用安全浏览，
     * 则不应调用此方法。 这会准备用于安全浏览的资源。
     */
    fun startSafeBrowsing(context: Context, callback: ValueCallback<Boolean>) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(context.applicationContext, callback);
        }
    }

}