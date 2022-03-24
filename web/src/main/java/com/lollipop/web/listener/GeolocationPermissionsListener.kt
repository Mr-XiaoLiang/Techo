package com.lollipop.web.listener

interface GeolocationPermissionsListener {

    fun request(origin: String, result: GeolocationPermissionsResult)

    fun abandon()

}

interface GeolocationPermissionsResult {
    /**
     * @param origin 请求来源
     * @param allow 是否同意
     * @param retain 是否在当前页面结束之前始终保持本次结果
     */
    fun onGeolocationPermissionsResult(origin: String, allow: Boolean, retain: Boolean)
}