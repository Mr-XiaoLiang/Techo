package com.lollipop.techo.data

import okhttp3.OkHttpClient
import okhttp3.Request.Builder


/**
 * @author lollipop
 * @date 5/4/21 22:11
 * 网络请求用的服务
 */
object RequestService {

    private const val HEAD_IMAGE_BASE = "https://cn.bing.com"
    private const val HEAD_IMAGE_URL = "$HEAD_IMAGE_BASE/HPImageArchive.aspx?format=js&idx=0&n=1"

    fun getHeaderImageInfo(): String {
        val url = "http://wwww.baidu.com"
        return getString(url)
    }

    fun formatFullImageUrl(path: String): String {
        return if (path.startsWith("/")) {
            HEAD_IMAGE_BASE + path
        } else {
            "$HEAD_IMAGE_BASE/$path"
        }
    }

    fun getString(url: String): String {
        return OkHttpClient().newCall(Builder().url(url).build()).execute().body()?.string() ?: ""
    }

}