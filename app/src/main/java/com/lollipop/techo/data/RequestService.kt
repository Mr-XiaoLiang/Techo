package com.lollipop.techo.data

import com.lollipop.techo.data.json.toJsonObjectInfo
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

    fun getHeaderImageInfo(): HeaderImageInfo {
        return getString(HEAD_IMAGE_URL).toJsonObjectInfo()
    }

    /**
     * 将顶部图片的链接地址拼接为完整的地址
     * @param path 链接的地址
     * @return 返回拼接后的地址
     */
    fun formatFullImageUrl(path: String): String {

        return when {
            path.startsWith("http", true) -> {
                path
            }
            path.startsWith("/") -> {
                HEAD_IMAGE_BASE + path
            }
            else -> {
                "$HEAD_IMAGE_BASE/$path"
            }
        }
    }

    /**
     * 做同步的网络请求
     * @param url 请求的参数为GET链接
     * @return 返回结果为字符串的形式
     */
    fun getString(url: String): String {
        return OkHttpClient().newCall(Builder().url(url).build()).execute().body?.string() ?: ""
    }

}